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

    # 크롤/파싱 실패로 판단(정책: 제목/본문 모두 없음)
    if not title and not text:
        raise HTTPException(
            status_code=500,
            detail={
                "error": "CRAWLING_FAILED",
                "message": "Velog 구조 변경 또는 예외로 인해 게시글을 파싱할 수 없습니다"
            },
        )

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

    if not data.get("posts"):
        raise HTTPException(
            status_code=500,
            detail={
                "error": "CRAWLING_FAILED",
                "message": "전달할 게시글이 없습니다. 사용자 또는 Velog 구조를 확인하세요"
            },
        )

    nlp_resp = await send_to_nlp(nlp_url, data)
    return len(data["posts"]), nlp_resp


async def crawl_and_store(username: str, resume_id: str, storage_url: str, body_max_posts: Optional[int]):
    data = await vc.crawl_all_posts(username)  # (limit_posts 쓰는 버전이면 적용)


    if not data.get("posts"):
        raise HTTPException(
            status_code=500,
            detail={
                "error": "CRAWLING_FAILED",
                "message": "수집된 게시글이 없습니다. 핸들이 올바른지 또는 Velog 구조 변경 여부를 확인하세요"
            },
        )

    payload = [
        {
            "url": p["url"],
            "link_type": "velog",
            "contents": {
                "title": p["title"], "text": p["text"], "tags": p["tags"],
                "code_langs": p["code_langs"], "published_at": p["published_at"],
                "content_hash": p["content_hash"], "source": "velog",
            },
            "resume_id": resume_id,
        }
        for p in data["posts"]
    ]

    storage_resp = await send_to_storage(storage_url, payload)
    return {"count": len(payload), "storage_response": storage_resp}
