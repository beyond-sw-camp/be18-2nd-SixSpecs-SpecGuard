import os, json, asyncio
from datetime import datetime, timedelta
from zoneinfo import ZoneInfo
from fastapi import HTTPException

from app.utils.codec import (
    to_gzip_base64_from_json, to_gzip_base64_from_text,
    to_gzip_bytes_from_json, to_gzip_bytes_from_text
)
from app.db import (
    SessionLocal,
    SQL_MARK_RESUME_CRAWLING, SQL_GET_VELOG_LINKS,
    SQL_CLAIM_LINK_RUNNING, SQL_SAVE_CONTENTS_COMPLETED, SQL_SET_FAILED_IF_RUNNING,
    SQL_COUNT_PENDING_LINKS, SQL_MARK_RESUME_PROCESSING,
    sql_save_contents_completed_blob, sql_set_nonexisted_blob   
)

SAVE_MODE = os.getenv("CONTENTS_SAVE_MODE", "BASE64").upper()      
BLOB_COL  = os.getenv("CONTENTS_BLOB_COLUMN", "contents_gzip")

SQL_SAVE_CONTENTS_COMPLETED_BLOB = sql_save_contents_completed_blob(BLOB_COL)
SQL_SET_NONEXISTED_BLOB          = sql_set_nonexisted_blob(BLOB_COL)

from app.crawlers import velog_crawler as vc
from app.utils.dates import normalize_created_at
from app.utils.codec import to_gzip_base64_from_json, to_gzip_base64_from_text

RECENT_WINDOW_DAYS = int(os.environ.get("RECENT_WINDOW_DAYS", "365"))
MAX_TEXT_LEN = int(os.environ.get("MAX_TEXT_LEN", "200000"))

def _recent_items_and_merged(posts: list[dict]):
    # 구조화
    items = []
    for p in posts:
        iso = normalize_created_at(p.get("published_at"))
        txt = p.get("text") or ""
        if MAX_TEXT_LEN and len(txt) > MAX_TEXT_LEN:
            txt = txt[:MAX_TEXT_LEN]
        items.append({"title": p.get("title") or "", "date": iso, "text": txt})

    # 최근 1년만
    cutoff = (datetime.now(ZoneInfo("Asia/Seoul")).date() - timedelta(days=RECENT_WINDOW_DAYS))
    items = [i for i in items if i["date"] and datetime.fromisoformat(i["date"]).date() >= cutoff]

    merged = []
    for i in items:
        merged.append(f"{i['date']} | [{i['title']}]\n{i['text']}".strip())
    return items, ("\n---\n".join(merged) if merged else "")

async def ingest_velog_for_resume(resume_id: str, _unused_url: str | None = None):
    """
    - resume.status: PENDING -> CRAWLING (CAS)
    - resume_link(velog) 각각:
        url 없으면 NONEXISTED(압축된 더미 텍스트 저장)
        url 있으면 RUNNING 선점 -> 크롤 -> gz(base64) 저장 -> COMPLETED
    - 모든 링크 종료 시 resume.status -> PROCESSING
    """
    # 1) 상태 전이(PENDING -> CRAWLING)
    async with SessionLocal() as sess:
        await sess.execute(SQL_MARK_RESUME_CRAWLING, {"rid": resume_id})
        await sess.commit()

        res = await sess.execute(SQL_GET_VELOG_LINKS, {"rid": resume_id})
        links = list(res.mappings())

    if not links:
        return {"handled": 0, "note": "no velog rows"}

    async def _handle_link(row):
        lid = row["id"]
        url = (row["url"] or "").strip()

        # URL 없음 -> NONEXISTED
        if not url:
            if SAVE_MODE == "GZIP_RAW":
                payload = to_gzip_bytes_from_text("제출된 링크 없음")
                async with SessionLocal() as s0:
                    await s0.execute(SQL_SET_NONEXISTED_BLOB, {"lid": lid, "contents": payload})
                    await s0.commit()
            else:
                dummy = to_gzip_base64_from_text("제출된 링크 없음")
                async with SessionLocal() as s0:
                    # 기존 JSON/TEXT 컬럼에 base64 문자열 저장
                    from app.db import SQL_SET_NONEXISTED_IF_NOT_TERMINAL
                    await s0.execute(SQL_SET_NONEXISTED_IF_NOT_TERMINAL, {"lid": lid, "contents": dummy})
                    await s0.commit()
            return

        # RUNNING 선점
        async with SessionLocal() as s1:
            result = await s1.execute(SQL_CLAIM_LINK_RUNNING, {"lid": lid})
            await s1.commit()
            if result.rowcount == 0:
                return

        try:
            crawled = await vc.crawl_all_with_url(url)
            posts = crawled.get("posts", [])
            post_count = int(crawled.get("post_count", len(posts)))
            items, merged = _recent_items_and_merged(posts)

            payload_dict = {
                "source": "velog",
                "base_url": url,
                "post_count": post_count,
                "recent_activity": merged,
                "recent_activity_items": items,
            }

            if SAVE_MODE == "GZIP_RAW":
                payload = to_gzip_bytes_from_json(payload_dict)
                async with SessionLocal() as s2:
                    await s2.execute(SQL_SAVE_CONTENTS_COMPLETED_BLOB, {"lid": lid, "contents": payload})
                    await s2.commit()
            else:
                gz_b64 = to_gzip_base64_from_json(payload_dict)
                async with SessionLocal() as s2:
                    await s2.execute(SQL_SAVE_CONTENTS_COMPLETED, {"lid": lid, "contents": gz_b64})
                    await s2.commit()
        except Exception:
            async with SessionLocal() as s3:
                await s3.execute(SQL_SET_FAILED_IF_RUNNING, {"lid": lid})
                await s3.commit()


    await asyncio.gather(*[_handle_link(r) for r in links])

    # 남은 진행건 없으면 이력서 상태 상향
    async with SessionLocal() as s_end:
        res2 = await s_end.execute(SQL_COUNT_PENDING_LINKS, {"rid": resume_id})
        remain = int(list(res2)[0][0])
        if remain == 0:
            await s_end.execute(SQL_MARK_RESUME_PROCESSING, {"rid": resume_id})
            await s_end.commit()

    return {"handled": len(links)}
