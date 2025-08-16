# app/crawlers/velog_crawler.py

from typing import List, Tuple, Optional, Set
from urllib.parse import urljoin
import time, re, hashlib

from playwright.sync_api import sync_playwright, TimeoutError as PWTimeout

# 공통 설정/상수
from . import CONF, LANG_NORMALIZE, WHITELIST, NOISE, UA
# utils 유틸은 당장 안 쓰면 주석 처리해도 OK
# from .utils import with_context, goto_with_retry, dump_on_fail


# 유저 글 목록(링크들) 수집
def render_list_with_playwright(
    handle: str,
    max_scrolls: int = CONF["list"]["max_scrolls"],
    pause_sec: float = CONF["list"]["pause_sec"],
    timeout_ms: int = CONF["list"]["timeout_ms"],
) -> List[str]:

    base = f"https://velog.io/@{handle}"
    hrefs: Set[str] = set()

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        ctx = browser.new_context(user_agent=UA, viewport={"width": 1280, "height": 900})

        # 이미지/폰트 차단 (속도 개선)
        try:
            ctx.route(
                "**/*",
                lambda route: route.abort()
                if route.request.resource_type in {"image", "font"}
                else route.continue_(),
            )
        except Exception:
            pass

        page = ctx.new_page()
        page.set_default_timeout(timeout_ms)
        page.set_default_navigation_timeout(timeout_ms)

        page.goto(base, wait_until="domcontentloaded")
        try:
            page.wait_for_load_state("networkidle", timeout=6000)
        except PWTimeout:
            pass

        def collect_links() -> Set[str]:
            anchors = page.locator("a").evaluate_all(
                "els => els.map(e => e.getAttribute('href') || '')"
            )
            out = set()
            for h in anchors:
                if not h:
                    continue
                if f"/@{handle}/" in h:
                    # 시리즈/태그/팔로워 등 제외
                    if any(x in h for x in ["/series/", "/tag/", "/followers", "/following"]):
                        continue
                    out.add(urljoin("https://velog.io", h))
            return out

        last_count = -1
        stagnant = 0
        for _ in range(max_scrolls):
            hrefs |= collect_links()

            if len(hrefs) == last_count:
                stagnant += 1
            else:
                stagnant = 0
            last_count = len(hrefs)

            if stagnant >= 3:
                break

            page.evaluate("window.scrollTo(0, document.body.scrollHeight)")
            time.sleep(pause_sec)
            try:
                page.wait_for_load_state("networkidle", timeout=3000)
            except PWTimeout:
                pass

        browser.close()

    hrefs = {h for h in hrefs if f"/@{handle}/" in h}
    return sorted(hrefs)


# 게시글 상세 수집
def render_post_with_playwright(
    url: str,
    timeout_ms: int = CONF["post"]["timeout_ms"],
) -> Tuple[str, str, List[str], List[str], Optional[str]]:

    start = time.perf_counter()
    HARD_LIMIT = max(8, timeout_ms / 1000 + 4)

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        ctx = browser.new_context(user_agent=UA, viewport={"width": 1280, "height": 900})

        # 이미지/폰트 차단
        try:
            ctx.route(
                "**/*",
                lambda route: route.abort()
                if route.request.resource_type in {"image", "font"}
                else route.continue_(),
            )
        except Exception:
            pass

        page = ctx.new_page()
        page.set_default_timeout(timeout_ms)
        page.set_default_navigation_timeout(timeout_ms)

        page.goto(url, wait_until="domcontentloaded")
        try:
            page.wait_for_load_state("networkidle", timeout=5000)
        except PWTimeout:
            pass

        # 제목
        title = ""
        try:
            title = page.locator("h1").first.inner_text().strip()
        except Exception:
            pass

        # 태그
        tags: List[str] = []
        try:
            tags = [t.strip() for t in page.locator("a[href*='/tag/']").all_inner_texts()]
        except Exception:
            pass

        # 코드 언어 추출 + 정규화
        code_langs: List[str] = []
        try:
            langs = page.eval_on_selector_all(
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

            # 정규화/필터
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
            if page.locator("article").count() > 0:
                text = page.locator("article").first.inner_text().strip()
        except Exception:
            pass
        if not text:
            for sel in ["main", "div#root", "body"]:
                try:
                    if page.locator(sel).count() > 0:
                        text = page.locator(sel).first.inner_text().strip()
                        if text:
                            break
                except Exception:
                    continue

        if not text and (time.perf_counter() - start) < HARD_LIMIT:
            try:
                page.evaluate("window.scrollTo(0, document.body.scrollHeight)")
                page.wait_for_load_state("networkidle", timeout=2000)
                if page.locator("article").count() > 0:
                    text = page.locator("article").first.inner_text().strip()
            except Exception:
                pass

        # 게시 시각(문자 그대로 저장)
        published = None
        try:
            for s in page.locator("time, span, div").all_inner_texts():
                s2 = s.strip()
                if re.search(r"\d{4}\.\s*\d{1,2}\.\s*\d{1,2}", s2) or ("시간 전" in s2) or ("분 전" in s2) or ("일 전" in s2):
                    published = s2
                    break
        except Exception:
            pass

        browser.close()
        return title, text, code_langs, tags, published


# 전체 파이프라인(선택)
def crawl_all_posts(
    handle: str,
    max_scrolls: int = 200,
    pause_sec: float = 1.0,
    per_post_delay: float = 1.0,
) -> dict:

    links = render_list_with_playwright(handle, max_scrolls=max_scrolls, pause_sec=pause_sec)
    print(f"[INFO] 링크 수집 완료: {len(links)}개")

    posts = []
    for i, url in enumerate(links, 1):
        try:
            title, text, code_langs, tags, published = render_post_with_playwright(url)

            if text:
                text = re.sub(r"(로그인|팔로우|목록 보기)\s*", " ", text)
                text = re.sub(r"\s{2,}", " ", text).strip()

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
                "content_hash": hashlib.md5(url.encode()).hexdigest(),
            })

            if i % 10 == 0:
                print(f"[INFO] {i}/{len(links)} 수집 중...")
            time.sleep(per_post_delay)
        except KeyboardInterrupt:
            print("\n[WARN] 사용자 중단 감지. 여기까지 저장합니다.")
            break
        except Exception as ex:
            print("skip:", url, ex)

    return {"source": "velog", "author": {"handle": handle}, "posts": posts, "schema_version": 1}


if __name__ == "__main__":
    import argparse, os, json

    parser = argparse.ArgumentParser(description="Velog full crawler")
    parser.add_argument("--handle", required=True, help="Velog handle (without @)")
    parser.add_argument("--max-scrolls", type=int, default=220)
    parser.add_argument("--pause", type=float, default=1.0)
    parser.add_argument("--per-post-delay", type=float, default=1.0)
    parser.add_argument("--out", default="out.json", help="output json path")
    parser.add_argument("--resume", action="store_true", help="skip already-scraped URLs from existing out.json")
    args = parser.parse_args()

    existing = {"source": "velog", "author": {"handle": args.handle}, "posts": [], "schema_version": 1}
    seen = set()
    if args.resume and os.path.exists(args.out):
        with open(args.out, encoding="utf-8") as f:
            try:
                prev = json.load(f)
                if prev.get("author", {}).get("handle") == args.handle:
                    existing = prev
                    seen = {p["url"] for p in prev.get("posts", [])}
            except Exception:
                pass

    data = crawl_all_posts(args.handle, max_scrolls=args.max_scrolls, pause_sec=args.pause, per_post_delay=args.per_post_delay)

    merged = existing.get("posts", []) + [p for p in data["posts"] if p["url"] not in seen]
    dedup = {p["url"]: p for p in merged}
    posts = list(dedup.values())

    out = {"source": "velog", "author": {"handle": args.handle}, "posts": posts, "schema_version": 1}
    with open(args.out, "w", encoding="utf-8") as f:
        json.dump(out, f, ensure_ascii=False, indent=2)
    print(f"[DONE] 총 {len(posts)}개 포스트 저장 → {args.out}")
