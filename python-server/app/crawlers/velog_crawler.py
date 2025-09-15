from typing import List, Tuple, Optional, Set
from urllib.parse import urlparse, urljoin
import re, asyncio, time, os, random, logging
from playwright.async_api import async_playwright, TimeoutError as PWTimeout

from . import CONF, UA
from app.utils.text import mask_pii, content_hash

log = logging.getLogger(__name__)

DELAY_LOW  = float(os.environ.get("CRAWL_DELAY_LOW_SEC", "0.8"))
DELAY_HIGH = float(os.environ.get("CRAWL_DELAY_HIGH_SEC", "2.0"))
MAX_CONCURRENCY = int(os.environ.get("CRAWLER_MAX_CONCURRENCY", "4"))

def _delay_range():
    pr = CONF["list"].get("pause_sec_range", (None, None))
    if pr and pr[0] is not None and pr[1] is not None:
        return pr
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
            await _sleep_with_jitter()
    raise last

_HANDLE_RE = re.compile(r"/@(?P<handle>[A-Za-z0-9_]{1,30})")

def _extract_handle_from_url(url: str) -> Optional[str]:
    try: p = urlparse(url)
    except Exception: return None
    m = _HANDLE_RE.search(p.path or "")
    return m.group("handle") if m else None

async def collect_post_links(ctx, base_url: str, max_scrolls: int) -> List[str]:
    page = await ctx.new_page()
    try:
        page.set_default_timeout(CONF["list"]["timeout_ms"])
        page.set_default_navigation_timeout(CONF["list"]["timeout_ms"])
        await _safe_goto(page, base_url)

        seen: Set[str] = set()
        hrefs: List[str] = []

        async def collect() -> List[str]:
            anchors = await page.eval_on_selector_all(
                "a", "els => els.map(e => e.getAttribute('href') || '')"
            )
            out: List[str] = []
            for h in anchors:
                if not h: continue
                full = urljoin("https://velog.io", h)
                if _HANDLE_RE.search(h) and h.count("/") >= 2:
                    if full not in seen:
                        seen.add(full)
                        out.append(full)
            return out

        last_count, stagnant = -1, 0
        for _ in range(max_scrolls):
            new_links = await collect()
            if new_links: hrefs.extend(new_links)

            if len(hrefs) == last_count: stagnant += 1
            else: stagnant = 0
            last_count = len(hrefs)

            if stagnant >= CONF["list"]["stagnant_rounds"]:
                break

            await page.evaluate("window.scrollTo(0, document.body.scrollHeight)")
            await _sleep_with_jitter()
        return hrefs
    finally:
        await page.close()

async def fetch_post(ctx, url: str) -> Tuple[str, str, List[str], List[str], Optional[str]]:
    start = time.perf_counter()
    HARD_LIMIT = max(8, CONF["post"]["timeout_ms"] / 1000 + CONF["post"]["hard_extra_sec"])
    page = await ctx.new_page()
    try:
        page.set_default_timeout(CONF["post"]["timeout_ms"])
        page.set_default_navigation_timeout(CONF["post"]["timeout_ms"])
        await _safe_goto(page, url)

        title = ""
        try:
            loc = page.locator("h1").first
            if await loc.count() > 0:
                title = (await loc.inner_text()).strip()
        except Exception:
            pass

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
                        if text: break
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

        published = None
        try:
            texts = await page.locator("time, span, div").all_inner_texts()
            for s in texts:
                s2 = s.strip()
                if re.search(r"\d{4}[.\-]\s*\d{1,2}[.\-]\s*\d{1,2}", s2) or \
                ("시간 전" in s2) or ("분 전" in s2) or ("일 전" in s2):
                    published = s2; break
        except Exception:
            pass

        if text:
            import re as _re
            text = _re.sub(r"(로그인|팔로우|목록 보기)\s*", " ", text)
            text = _re.sub(r"\s{2,}", " ", text).strip()
            text = mask_pii(text)

        return title or "", text or "", [], tags, published
    finally:
        await page.close()

async def _crawl_all_with_url_async(base_url: str) -> dict:
    """
    실제 Playwright 크롤링을 수행하는 비동기 함수.
    이 함수는 Proactor 루프에서 실행되어야 함(아래 _worker_thread에서 보장).
    """
    handle = _extract_handle_from_url(base_url) or ""
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        try:
            ctx = await browser.new_context(user_agent=UA, viewport=CONF["viewport"])
            await _block_heavy_assets(ctx)

            links = await collect_post_links(ctx, base_url, CONF["list"]["max_scrolls"])
            post_count = len(links)

            sem = asyncio.Semaphore(MAX_CONCURRENCY)
            results = []

            async def _one(u: str):
                async with sem:
                    try:
                        t, txt, _, tags, pub = await fetch_post(ctx, u)
                        results.append((u, t, txt, tags, pub))
                    except Exception:
                        log.exception("Velog crawl skip: %s", u)

            await asyncio.gather(*(_one(u) for u in links))

            posts = []
            for (u, title, text, tags, published) in results:
                posts.append({
                    "url": u,
                    "title": title,
                    "published_at": published or "",
                    "text": text or "",
                    "tags": tags or [],
                    "content_hash": content_hash(text or "", fallback=u),
                })

            return {"source": "velog", "author": {"handle": handle}, "posts": posts, "post_count": post_count}
        finally:
            await browser.close()


def _worker_thread(base_url: str) -> dict:
    """
    별도 스레드에서 실행: Windows일 때 Proactor 정책을 강제하고,
    그 전용 이벤트 루프에서 _crawl_all_with_url_async()를 실행.
    """
    import sys, asyncio
    if sys.platform.startswith("win"):
        asyncio.set_event_loop_policy(asyncio.WindowsProactorEventLoopPolicy())
    loop = asyncio.new_event_loop()
    try:
        asyncio.set_event_loop(loop)
        return loop.run_until_complete(_crawl_all_with_url_async(base_url))
    finally:
        # 잔여 태스크 정리
        try:
            loop.run_until_complete(asyncio.sleep(0))
        except Exception:
            pass
        loop.close()


async def crawl_all_with_url(base_url: str) -> dict:
    """
    서비스에서 호출하는 공개 API.
    메인 이벤트 루프(Selector일 수도 있음)와 분리하기 위해
    '스레드 실행자'에서 Playwright를 돌린다.
    """
    loop = asyncio.get_running_loop()
    return await loop.run_in_executor(None, lambda: _worker_thread(base_url))
