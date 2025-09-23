import os
import httpx, logging, os
from datetime import datetime, timedelta
from zoneinfo import ZoneInfo
from fastapi import HTTPException

from app.crawlers import velog_crawler as vc



DEBUG_RETURN = os.getenv("CRAWLER_DEBUG_RETURN", "0") == "1"  # 반환 토글
DEBUG_LOG    = os.getenv("CRAWLER_DEBUG_LOG", "0") == "1" 


from app.db import (
    SessionLocal,
    SQL_FIND_RESUME_LINK_ID,
    SQL_CLAIM_RUNNING,
    SQL_SET_NOTEXISTED_IF_NOT_TERMINAL,
    SQL_SAVE_COMPLETED,
    SQL_SET_FAILED_IF_RUNNING,
)
from app.crawlers import velog_crawler as vc
from app.utils.dates import normalize_created_at
from app.utils.codec import to_gzip_bytes_from_json, to_gzip_bytes_from_text

RECENT_WINDOW_DAYS = int(os.getenv("RECENT_WINDOW_DAYS", "365"))
MAX_TEXT_LEN = int(os.getenv("MAX_TEXT_LEN", "200000"))
RL_TYPE_VELOG = os.getenv("RL_VELOG_TYPE", "VELOG")
LOCAL_TZ = os.getenv("LOCAL_TZ", "Asia/Seoul")


def _today_local_date():
    """환경 타임존 기준 오늘 날짜. 실패 시 로컬 날짜."""
    try:
        return datetime.now(ZoneInfo(LOCAL_TZ)).date()
    except Exception:
        return datetime.now().date()


def _build_recent_activity(posts: list[dict]) -> str:
    """
    크롤링된 posts에서 최근 N일(RECENT_WINDOW_DAYS) 내 글만 뽑아
    'YYYY-MM-DD | [제목]\n본문' 형태로 병합한 큰 텍스트를 만든다.
    """
    items: list[tuple[str, str, str]] = []

    for p in posts:
        iso = normalize_created_at(p.get("published_at"), tz=LOCAL_TZ)
        if not iso:
            continue
        text = (p.get("text") or "").strip()
        if not text:
            continue  # 본문이 비어있으면 제외
        if MAX_TEXT_LEN and len(text) > MAX_TEXT_LEN:
            text = text[:MAX_TEXT_LEN]
        title = (p.get("title") or "").strip()
        items.append((iso, title, text))

    if not items:
        return ""

    # 최근 N일 컷오프
    cutoff = _today_local_date() - timedelta(days=RECENT_WINDOW_DAYS)

    # 튜플 인덱싱으로 안전 필터
    filtered: list[tuple[str, str, str]] = []
    for (d, t, c) in items:
        try:
            if datetime.fromisoformat(d).date() >= cutoff:
                filtered.append((d, t, c))
        except Exception:
            # 날짜 파싱 실패시 해당 항목 스킵
            continue

    if not filtered:
        return ""

    # 문자열 병합
    return "\n---\n".join([f"{d} | [{t}]\n{c}".strip() for (d, t, c) in filtered])


def _count_recent_posts(posts: list[dict], *, days: int, tz: str) -> int:
    """published_at 기준으로 최근 N일 이내 글 개수"""
    cutoff = (datetime.now(ZoneInfo(tz)).date() if tz else datetime.now().date()) - timedelta(days=days)
    cnt = 0
    for p in posts:
        iso = normalize_created_at(p.get("published_at"), tz=tz)
        if not iso:
            continue
        try:
            if datetime.fromisoformat(iso).date() >= cutoff:
                cnt += 1
        except Exception:
            continue
    return cnt



async def ingest_velog_single(resume_id: str, url: str | None):
    url = (url or "").strip()

    # 대상 resume_link.id 찾기 (없으면 404)
    async with SessionLocal() as s:
        res = await s.execute(
            SQL_FIND_RESUME_LINK_ID,
            {"rid": resume_id, "lt": RL_TYPE_VELOG, "url": url},
        )
        row = res.mappings().first()

    if not row:
        if not url:   # url이 None → "" 변환된 케이스
                    return {"claimed": False, "status": "NOTEXISTED"}
        # 주어진 resume_id/url로 VELOG 유형의 링크 행을 못 찾음
        raise HTTPException(
            status_code=404,
            detail={"errorCode": "NOT_FOUND", "message": "resume_link(row) not found for given resume_id/url"},
        )

    lid = row["id"]

    # URL 공란이면: NOTEXISTED + 더미 gzip 후 종료
    if not url:
        payload = {
            "source": "velog",
            "base_url": "",
            "post_count": 0,
            "recent_activity": ""
        }
        dummy = to_gzip_bytes_from_json(payload)

        
        async with SessionLocal() as s0:
            await s0.execute(
                SQL_SET_NOTEXISTED_IF_NOT_TERMINAL,
                {"rid": resume_id, "lid": lid, "contents": dummy},
            )
            await s0.commit()
        return {"claimed": False, "status": "NOTEXISTED"}

    # RUNNING 선점 (PENDING -> RUNNING)
    async with SessionLocal() as s1:
        r = await s1.execute(SQL_CLAIM_RUNNING, {"rid": resume_id, "lid": lid})
        await s1.commit()
        if r.rowcount == 0:
            # 이미 RUNNING/COMPLETED/FAILED/NOTEXISTED 등
            return {"claimed": False, "status": "SKIPPED"}

    # 실제 크롤링
    try:
        crawled = await vc.crawl_all_with_url(url)
        posts = crawled.get("posts", [])
        post_count = int(crawled.get("post_count", len(posts)))

        recent_count = _count_recent_posts(
            posts,
            days=RECENT_WINDOW_DAYS,
            tz=LOCAL_TZ,
        )

        recent_activity = _build_recent_activity(posts)

        payload = {
            "source": "velog",
            "base_url": url,
            "post_count": post_count,
            "recent_count": recent_count,
            "recent_activity": recent_activity,
        }


        if DEBUG_RETURN:
                    return {"status": "DEBUG", "data": payload}


        gz = to_gzip_bytes_from_json(payload)

        # RUNNING -> COMPLETED + gzip 저장
        async with SessionLocal() as s2:
            await s2.execute(
                SQL_SAVE_COMPLETED,
                {"rid": resume_id, "lid": lid, "contents": gz},
            )
            await s2.commit()

        return {"claimed": True, "status": "COMPLETED", "post_count": post_count}

    except Exception as e:
        # RUNNING -> FAILED
        async with SessionLocal() as s3:
            await s3.execute(SQL_SET_FAILED_IF_RUNNING, {"rid": resume_id, "lid": lid})
            await s3.commit()
        raise HTTPException(
            status_code=500,
            detail={"errorCode": "CRAWLING_FAILED", "message": str(e)},
        )
