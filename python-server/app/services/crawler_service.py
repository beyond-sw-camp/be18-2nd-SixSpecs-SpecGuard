# app/services/crawler_service.py
import os, asyncio
from datetime import datetime, timedelta
from zoneinfo import ZoneInfo
from fastapi import HTTPException

from app.db import (
    SessionLocal,
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

def _recent_items_and_merged(posts: list[dict]):
    items = []
    for p in posts:
        iso = normalize_created_at(p.get("published_at"))
        txt = p.get("text") or ""
        if MAX_TEXT_LEN and len(txt) > MAX_TEXT_LEN:
            txt = txt[:MAX_TEXT_LEN]
        items.append({"title": p.get("title") or "", "date": iso, "text": txt})

    cutoff = (datetime.now(ZoneInfo("Asia/Seoul")).date() - timedelta(days=RECENT_WINDOW_DAYS))
    items = [i for i in items if i["date"] and datetime.fromisoformat(i["date"]).date() >= cutoff]

    merged = []
    for i in items:
        merged.append(f"{i['date']} | [{i['title']}]\n{i['text']}".strip())
    return items, ("\n---\n".join(merged) if merged else "")

async def ingest_velog_single(resume_id: str, url: str):
    url = (url or "").strip()

    # URL 공란이면: NOTEXISTED + 더미 gzip 후 종료
    if not url:
        dummy = to_gzip_bytes_from_text("제출된 링크 없음")
        async with SessionLocal() as s0:
            await s0.execute(
                SQL_SET_NOTEXISTED_IF_NOT_TERMINAL,
                {"rid": resume_id, "url": "", "contents": dummy},
            )
            await s0.commit()
        return {"claimed": False, "status": "NOTEXISTED"}

    # RUNNING 선점 (PENDING -> RUNNING)
    async with SessionLocal() as s1:
        r = await s1.execute(SQL_CLAIM_RUNNING, {"rid": resume_id, "url": url})
        await s1.commit()
        if r.rowcount == 0:
            # 이미 RUNNING/COMPLETED/FAILED/NOTEXISTED 등
            return {"claimed": False, "status": "SKIPPED"}

    # 실제 크롤링
    try:
        crawled = await vc.crawl_all_with_url(url)
        posts = crawled.get("posts", [])
        post_count = int(crawled.get("post_count", len(posts)))
        items, merged = _recent_items_and_merged(posts)

        payload = {
            "source": "velog",
            "base_url": url,
            "post_count": post_count,
            "recent_activity": merged,
            "recent_activity_items": items,
        }
        gz = to_gzip_bytes_from_json(payload)

        # RUNNING -> COMPLETED + gzip 저장
        async with SessionLocal() as s2:
            await s2.execute(
                SQL_SAVE_COMPLETED, {"rid": resume_id, "url": url, "contents": gz}
            )
            await s2.commit()

        return {"claimed": True, "status": "COMPLETED", "post_count": post_count}
    except Exception as e:
        # RUNNING -> FAILED
        async with SessionLocal() as s3:
            await s3.execute(SQL_SET_FAILED_IF_RUNNING, {"rid": resume_id, "url": url})
            await s3.commit()
        raise HTTPException(status_code=500, detail={"error": "CRAWLING_FAILED", "message": str(e)})
