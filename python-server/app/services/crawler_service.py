import os
from typing import Optional
from app.crawlers import velog_crawler as vc
from app.utils.dates import normalize_created_at
from app.services.nlp_client import send_to_nlp

def _env_bool(key: str, default: str = "false") -> bool:
    return os.environ.get(key, default).lower() in {"1", "true", "yes"}

def _env_int(key: str, default: str) -> int:
    try:
        return int(os.environ.get(key, default))
    except Exception:
        return int(default)

# 목록
async def list_posts(username: str, page: int, limit: int):
    links: List[str] = await vc.render_list_with_playwright(username)
    if not links:
        return []

    start, end = (page - 1) * limit, (page - 1) * limit + limit
    out = []
    for url in links[start:end]:
        title, _text, _langs, tags, published = await vc.render_post_with_playwright(url)
        out.append({
            "title": title or "",
            "url": url,
            "date": normalize_created_at(published),
            "tags": tags or [],
        })
    return out

# 상세
async def post_detail(url: str):
    title, text, langs, tags, published = await vc.render_post_with_playwright(url)
    MAX_TEXT_LEN = _env_int("MAX_TEXT_LEN", "200000")
    if text and len(text) > MAX_TEXT_LEN:
        text = text[:MAX_TEXT_LEN]
    return {
        "title": title,
        "url": url,
        "createdAt": normalize_created_at(published),
        "content": text or "",
        "tags": tags or [],
        "codeLangs": langs or [],
    }

async def crawl_and_forward(username: str, nlp_url: str, body_max_posts: Optional[int]):
    data = await vc.crawl_all_posts(username)

    # ========테스트용========
    # force_full = _env_bool("FORCE_FULL_CRAWL", "false")
    # if not force_full:
    #     default_max = _env_int("DEFAULT_CRAWL_MAX_POSTS", "10")
    #     max_posts = body_max_posts if (body_max_posts is not None) else default_max
    #     if max_posts and max_posts > 0:
    #         data = {**data, "posts": data["posts"][: max_posts]}

    # 전체 내용 크롤링 (테스트용 할거면 이 부분 주석처리)
    force_full = True


    nlp_resp = await send_to_nlp(nlp_url, data)
    return len(data["posts"]), nlp_resp