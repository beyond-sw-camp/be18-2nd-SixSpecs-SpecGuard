# app/crawlers/velog_crawler.py
from typing import List, Tuple, Optional, Set
from urllib.parse import urljoin
import re, asyncio, time, os, random
import logging
log = logging.getLogger(__name__)

from playwright.async_api import async_playwright, TimeoutError as PWTimeout

from . import CONF, LANG_NORMALIZE, WHITELIST, NOISE, UA
from app.utils.text import mask_pii, content_hash

# 랜덤 딜레이(트래픽에 따라 env로 조정)
DELAY_LOW = float(os.environ.get("CRAWL_DELAY_LOW_SEC", "0.8"))
DELAY_HIGH = float(os.environ.get("CRAWL_DELAY_HIGH_SEC", "2.0"))

def _delay_range():
    # 1) CONF에 범위가 있으면 최우선
    pr = CONF["list"].get("pause_sec_range", (None, None))
    if pr and pr[0] is not None and pr[1] is not None:
        return pr
    # 2) ENV (기본값 0.8~2.0초)
    return (DELAY_LOW, DELAY_HIGH)

async def _sleep_with_jitter():
    low, high = _delay_range()
    if low is None or high is None:
        await asyncio.sleep(CONF["list"]["pause_sec"])
    else:
        await asyncio.sleep(random.uniform(low, high))

async def _block_heavy_assets(ctx):
    async def _route(route):
        rtype = route.request.resource_type
        if rtype in {"image", "font"}:
            await route.abort()
        else:
            await route.continue_()
    await ctx.route("**/*", _route)

async def _safe_goto(page, url, retries=2, wait="domcontentloaded"):
    last = None
    for _ in range(retries + 1):
        try:
            await page.goto(url, wait_until=wait)
            try:
                await page.wait_for_load_state("networkidle", timeout=5000)
            except PWTimeout:
                pass
            return
        except Exception as e:
            last = e
            await _sleep_with_jitter()  # ← 백오프도 지터 사용
    raise last

# 유저 글 목록(링크들) 수집
async def render_list_with_playwright(
    handle: str,
    max_scrolls: int = CONF["list"]["max_scrolls"],
    pause_sec: float = CONF["list"]["pause_sec"],   # 호환을 위해 남겨두지만, 지터가 우선 적용됩니다.
    timeout_ms: int = CONF["list"]["timeout_ms"],
) -> List[str]:

    base = f"https://velog.io/@{handle}"
    hrefs: Set[str] = set()

    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        try:
            ctx = await browser.new_context(user_agent=UA, viewport=CONF["viewport"])
            await _block_heavy_assets(ctx)

            page = await ctx.new_page()
            page.set_default_timeout(timeout_ms)
            page.set_default_navigation_timeout(timeout_ms)

            await _safe_goto(page, base)

            async def collect_links() -> Set[str]:
                anchors = await page.eval_on_selector_all(
                    "a", "els => els.map(e => e.getAttribute('href') || '')"
                )
                out = set()
                for h in anchors or []:
                    if not h:
                        continue
                    if f"/@{handle}/" in h:
                        if any(x in h for x in ["/series/", "/tag/", "/followers", "/following"]):
                            continue
                        out.add(urljoin("https://velog.io", h))
                return out

            last_count, stagnant = -1, 0
            for _ in range(max_scrolls):
                hrefs |= await collect_links()

                if len(hrefs) == last_count:
                    stagnant += 1
                else:
                    stagnant = 0
                last_count = len(hrefs)

                if stagnant >= CONF["list"]["stagnant_rounds"]:
                    break

                await page.evaluate("window.scrollTo(0, document.body.scrollHeight)")
                await _sleep_with_jitter()  # ← 여기서 지터 사용

        finally:
            await browser.close()

    hrefs = {h for h in hrefs if f"/@{handle}/" in h}
    return sorted(hrefs)

# 게시글 상세 수집
async def render_post_with_playwright(
    url: str,
    timeout_ms: int = CONF["post"]["timeout_ms"],
) -> Tuple[str, str, List[str], List[str], Optional[str]]:

    start = time.perf_counter()
    HARD_LIMIT = max(8, timeout_ms / 1000 + CONF["post"]["hard_extra_sec"])

    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        try:
            ctx = await browser.new_context(user_agent=UA, viewport=CONF["viewport"])
            await _block_heavy_assets(ctx)

            page = await ctx.new_page()
            page.set_default_timeout(timeout_ms)
            page.set_default_navigation_timeout(timeout_ms)

            await _safe_goto(page, url)

            # 제목
            title = ""
            try:
                loc = page.locator("h1").first
                if await loc.count() > 0:
                    title = (await loc.inner_text()).strip()
            except Exception:
                pass

            # 태그 (안정화된 셀렉터 + 폴백)
            tags: List[str] = []
            try:
                tags = await page.evaluate("""
                    () => {
                        const out = new Set();
                        const pick = (txt) => {
                            if (!txt) return;
                            const t = txt.trim().replace(/^#/, "");
                            if (t && t.length <= 50) out.add(t);
                        };
                        document.querySelectorAll(
                            'a[href^="/tags/"], a[href*="/tag/"], a[class*="tag"], a[class*="Tag"]'
                        ).forEach(a => pick(a.textContent));
                        document.querySelectorAll('meta[property="article:tag"]').forEach(m => {
                            const c = m.getAttribute("content");
                            pick(c);
                        });
                        return Array.from(out);
                    }
                """) or []
                tags = sorted({t.strip() for t in tags if t and t.strip()})
            except Exception:
                pass

            # 코드 언어 추출 + 정규화
            code_langs: List[str] = []
            try:
                langs = await page.eval_on_selector_all(
                    "pre code",
                    """els => els.map(e => {
                        const cls = (e.className||"").toString();
                        let lang = null;
                        const m = cls.match(/language-([\\w+-]+)/);
                        if (m) lang = m[1].toLowerCase();
                        const dl = e.getAttribute('data-language');
                        if (dl) lang = dl.toLowerCase();
                        return lang;
                    })""",
                ) or []

                langs = [l for l in langs if l and l != "null"]
                langs = [LANG_NORMALIZE.get(l.strip().lower(), l.strip().lower()) for l in langs]
                langs = [l for l in langs if l not in NOISE]
                langs = [l for l in langs if l in WHITELIST]
                code_langs = sorted(set(langs))
            except Exception:
                pass

            # 본문
            text = ""
            try:
                if await page.locator("article").count() > 0:
                    text = (await page.locator("article").first.inner_text()).strip()
            except Exception:
                pass

            if not text:
                for sel in ["main", "div#root", "body"]:
                    try:
                        if await page.locator(sel).count() > 0:
                            text = (await page.locator(sel).first.inner_text()).strip()
                            if text:
                                break
                    except Exception:
                        continue

            if not text and (time.perf_counter() - start) < HARD_LIMIT:
                try:
                    await page.evaluate("window.scrollTo(0, document.body.scrollHeight)")
                    await page.wait_for_load_state("networkidle", timeout=2000)
                    if await page.locator("article").count() > 0:
                        text = (await page.locator("article").first.inner_text()).strip()
                except Exception:
                    pass

            # 게시 시각(문자 그대로 저장)
            published = None
            try:
                texts = await page.locator("time, span, div").all_inner_texts()
                for s in texts:
                    s2 = s.strip()
                    if re.search(r"\d{4}[.\-]\s*\d{1,2}[.\-]\s*\d{1,2}", s2) or \
                       ("시간 전" in s2) or ("분 전" in s2) or ("일 전" in s2):
                        published = s2
                        break
            except Exception:
                pass

        finally:
            await browser.close()

    return title or "", text or "", code_langs, tags, published

# 전체 파이프라인
async def crawl_all_posts(
    handle: str,
    max_scrolls: int = CONF["list"]["max_scrolls"],
    pause_sec: float = CONF["list"]["pause_sec"],  # 호환 위해 남겨둠
    per_post_delay: float = 1.0,                   # 외부에서 고정 지연 쓰면 이 값 사용
) -> dict:

    links = await render_list_with_playwright(handle, max_scrolls=max_scrolls, pause_sec=pause_sec)
    posts = []
    for i, url in enumerate(links, 1):
        try:
            title, text, code_langs, tags, published = await render_post_with_playwright(url)

            if text:
                text = re.sub(r"(로그인|팔로우|목록 보기)\s*", " ", text)
                text = re.sub(r"\s{2,}", " ", text).strip()
                text = mask_pii(text)

            posts.append({
                "url": url,
                "title": title or "",
                "tags": tags,
                "published_at": published or "",
                "updated_at": "",
                "text": text or "",
                "code_langs": code_langs,
                "likes": 0,
                "comments": 0,
                "series": None,
                "content_hash": content_hash(text or "", fallback=url),
            })

            if i % 10 == 0:
                log.info("[%d/%d] 수집 중...", i, len(links))

            # per_post_delay가 명시되면 우선 사용, 아니면 지터 사용
            if per_post_delay and per_post_delay > 0:
                await asyncio.sleep(per_post_delay)
            else:
                await _sleep_with_jitter()

        except Exception:
            log.exception("Velog crawl skip: %s", url)

    return {"source": "velog", "author": {"handle": handle}, "posts": posts, "schema_version": 1}
