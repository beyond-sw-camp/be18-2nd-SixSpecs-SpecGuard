import os
from typing import Optional, List, Dict, Any
from fastapi import HTTPException

from app.crawlers import velog_crawler as vc
from app.utils.dates import normalize_created_at
from app.services.nlp_client import send_to_nlp
from app.services.storage_client import send_to_storage


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
        out.append(
            {
                "title": title or "",
                "url": url,
                "date": normalize_created_at(published),
                "tags": tags or [],
            }
        )
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


# NLP로 전체/일부 전송
async def crawl_and_forward(username: str, nlp_url: str, body_max_posts: Optional[int]):
    data = await vc.crawl_all_posts(username)

    # 요청에 max_posts가 오면 그 수만큼만 전송
    max_posts = body_max_posts or 0
    if max_posts > 0:
        data = {**data, "posts": data["posts"][:max_posts]}

    nlp_resp = await send_to_nlp(nlp_url, data)
    return len(data["posts"]), nlp_resp


# 저장 서버로 전송 (ERD: resume_link)
async def crawl_and_store(
    username: str, resume_id: str, storage_url: str, body_max_posts: Optional[int]
) -> Dict[str, Any]:
    # 1) 전체 크롤링
    data = await vc.crawl_all_posts(username)

    # 2) max_posts 적용 (양수일 때만)
    max_posts = body_max_posts or 0
    if max_posts > 0:
        data = {**data, "posts": data["posts"][:max_posts]}

    # 3) resume_link 형태로 변환
    records: List[Dict[str, Any]] = []
    for p in data["posts"]:
        records.append(
            {
                "url": p["url"],
                "link_type": "velog",
                "contents": {
                    "title": p["title"],
                    "text": p["text"],
                    "tags": p["tags"],
                    "code_langs": p["code_langs"],
                    "published_at": p["published_at"],
                    "content_hash": p["content_hash"],
                    "source": data["source"],
                },
                "resume_id": resume_id,
            }
        )
    payload = {"resume_id": resume_id, "records": records}

    # 4) 저장 서버 호출
    try:
        storage_resp = await send_to_storage(storage_url, payload)
    except Exception as e:
        # 저장 서버가 4xx/5xx 또는 네트워크 오류일 때: 502로 래핑
        raise HTTPException(
            status_code=502,
            detail={"error": "STORAGE_SERVER_ERROR", "message": str(e)},
        )

    return {"count": len(records), "storage_response": storage_resp}
