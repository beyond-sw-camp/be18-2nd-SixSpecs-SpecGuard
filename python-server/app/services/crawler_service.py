import os
import asyncio
from typing import Optional, List, Dict, Any
from fastapi import HTTPException

from app.crawlers import velog_crawler as vc
from app.utils.dates import normalize_created_at
from app.services.nlp_client import send_to_nlp
from app.services.storage_client import send_to_storage


# ENV helpers
def _env_bool(key: str, default: str = "false") -> bool:
    return os.environ.get(key, default).lower() in {"1", "true", "yes"}

def _env_int(key: str, default: str) -> int:
    try:
        return int(os.environ.get(key, default))
    except Exception:
        return int(default)

def _env_float(key: str, default: str) -> float:
    try:
        return float(os.environ.get(key, default))
    except Exception:
        return float(default)


# Chunk helpers
def _chunked(seq: List[Any], size: int):
    """size 단위로 리스트를 잘라 제너레이터로 반환"""
    for i in range(0, len(seq), size):
        yield seq[i:i + size]

async def _sleep_ms(ms: int | float):
    if ms and ms > 0:
        await asyncio.sleep(float(ms) / 1000.0)


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
                "message": "Velog 구조 변경 또는 예외로 인해 게시글을 파싱할 수 없습니다",
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


# NLP로 전체/일부 전송 (배치 전송)
async def crawl_and_forward(username: str, nlp_url: str, body_max_posts: Optional[int]):
    data = await vc.crawl_all_posts(username)
    posts = data.get("posts") or []

    if not posts:
        raise HTTPException(
            status_code=500,
            detail={"error": "CRAWLING_FAILED", "message": "전달할 게시글이 없습니다. 사용자 또는 Velog 구조를 확인하세요"},
        )

    # 상한(요청 바디 또는 환경변수) 적용
    max_posts = body_max_posts or _env_int("DEFAULT_CRAWL_MAX_POSTS", "0")
    if max_posts and max_posts > 0:
        posts = posts[:max_posts]

    # 배치 크기 및 배치 사이 슬립(ms)
    batch_size = _env_int("NLP_BATCH_SIZE", "100")  # 1 이하면 단일 전송
    batch_sleep_ms = _env_int("NLP_BATCH_SLEEP_MS", "0")

    # 단일 전송 경로
    if batch_size <= 1 or len(posts) <= batch_size:
        payload = {**data, "posts": posts}
        resp = await send_to_nlp(nlp_url, payload)
        return len(posts), resp

    # 배치 전송 경로
    total = len(posts)
    ok_batches = 0
    fail_batches = 0
    results: List[Dict[str, Any]] = []

    for batch in _chunked(posts, batch_size):
        payload = {**data, "posts": batch}
        try:
            resp = await send_to_nlp(nlp_url, payload)
            results.append({"ok": True, "size": len(batch), "resp": resp})
            ok_batches += 1
        except Exception as e:
            results.append({"ok": False, "size": len(batch), "error": str(e)})
            fail_batches += 1
        # 배치 간 슬립(옵션)
        await _sleep_ms(batch_sleep_ms)

    # 모든 배치 실패 시 에러 반환
    if ok_batches == 0:
        raise HTTPException(
            status_code=502,
            detail={"error": "UPSTREAM_FAILED", "message": "NLP 서버 전송이 모두 실패했습니다"},
        )

    # 응답은 기존 스펙을 유지하면서 요약 제공
    summary = {
        "batches": ok_batches + fail_batches,
        "ok": ok_batches,
        "fail": fail_batches,
    }
    return total, summary


# 스토리지로 저장 요청 (배치 전송)
async def crawl_and_store(username: str, resume_id: str, storage_url: str, body_max_posts: Optional[int]):
    data = await vc.crawl_all_posts(username)
    posts = data.get("posts") or []

    if not posts:
        raise HTTPException(
            status_code=500,
            detail={"error": "CRAWLING_FAILED", "message": "수집된 게시글이 없습니다. 핸들이 올바른지 또는 Velog 구조 변경 여부를 확인하세요"},
        )

    # 상한(요청 바디 또는 환경변수) 적용
    max_posts = body_max_posts or _env_int("DEFAULT_CRAWL_MAX_POSTS", "0")
    if max_posts and max_posts > 0:
        posts = posts[:max_posts]

    # 배치 크기 및 배치 사이 슬립(ms)
    batch_size = _env_int("STORAGE_BATCH_SIZE", "200")  # 1 이하면 단일 전송
    batch_sleep_ms = _env_int("STORAGE_BATCH_SLEEP_MS", "0")

    def to_payload(batch: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """스토리지 서버 명세에 맞는 배열 형태로 변환"""
        return [
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
                    "source": "velog",
                },
                "resume_id": resume_id,
            }
            for p in batch
        ]

    # 단일 전송 경로
    if batch_size <= 1 or len(posts) <= batch_size:
        storage_resp = await send_to_storage(storage_url, to_payload(posts))
        return {"count": len(posts), "storage_response": storage_resp}

    # 배치 전송 경로
    total_sent = 0
    ok_batches = 0
    fail_batches = 0
    for batch in _chunked(posts, batch_size):
        try:
            _ = await send_to_storage(storage_url, to_payload(batch))
            ok_batches += 1
            total_sent += len(batch)
        except Exception:
            fail_batches += 1
        # 배치 간 슬립(옵션)
        await _sleep_ms(batch_sleep_ms)

    if ok_batches == 0:
        raise HTTPException(
            status_code=502,
            detail={"error": "UPSTREAM_FAILED", "message": "스토리지 서버 전송이 모두 실패했습니다"},
        )

    return {
        "count": total_sent,
        "storage_response": {
            "batches": ok_batches + fail_batches,
            "ok": ok_batches,
            "fail": fail_batches,
        },
    }
