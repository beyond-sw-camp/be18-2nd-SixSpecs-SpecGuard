from typing import Optional
from fastapi import APIRouter, HTTPException, Path, Body, Query
from pydantic import BaseModel, Field
from app.services import crawler_service as svc
from app.crawlers import velog_crawler as vc      
from app.utils.dates import normalize_created_at 
from base64 import b64encode
import gzip, json
import tzdata
from datetime import datetime, timedelta
import os
import math
LOCAL_TZ = os.getenv("LOCAL_TZ", "Asia/Seoul")

router = APIRouter(prefix="/api/v1", tags=["ingest"])
RECENT_WINDOW_DAYS = int(os.getenv("RECENT_WINDOW_DAYS", "365"))
@router.get("/debug/velog")
async def debug_velog(url: str = Query(..., description="Velog 프로필 URL (예: https://velog.io/@handle/posts)")):
    """
    DB 업데이트 없이, 크롤링 '생(raw)' 결과를 바로 확인하는 디버그 엔드포인트.
    """
    try:
        crawled = await vc.crawl_all_with_url(url)
        posts = crawled.get("posts", [])
        recent_count = svc._count_recent_posts(posts, days=RECENT_WINDOW_DAYS, tz=LOCAL_TZ)
        recent_count = math.floor((recent_count + 1) / 2)
        # recent_activity 형식
        lines = []
        for p in posts:
            d = normalize_created_at(p.get("published_at")) or ""
            t = p.get("title") or ""
            c = (p.get("text") or "").strip()
            lines.append(f"{d} | [{t}]\n{c}")
        recent_activity = "\n---\n".join(lines)
        if(len(posts) < recent_count):
            recent_count = len(posts)
    

        return {
            "status": "debug",
            "data": {
                "source": "velog",
                "base_url": url,
                "post_count": int(crawled.get("post_count", len(posts))),
                "recent_activity": recent_activity,
                "posts": posts,
            }
        }
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail={"error":"CRAWLING_FAILED", "message": str(e)})


class StartBody(BaseModel):
    url: Optional[str] = Field(None, description="Velog 프로필 URL (예: https://velog.io/@handle/posts)")

@router.post("/ingest/resumes/{resumeId}/velog/start")
async def start_velog_ingest(
    resumeId: str = Path(..., description="resume.id (UUID)"),
    body: StartBody = Body(...),
):
    try:
        result = await svc.ingest_velog_single(resumeId, body.url)
        return {"status": "success", "data": result}
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail={"errorCode":"INTERNAL_SERVER_ERROR", "message": str(e)})
    


#압축 해제 확인

@router.get("/debug/load")
async def debug_get_payload(
    url: str = Query(..., description="Velog 프로필 URL (예: https://velog.io/@handle/posts)")
):
    """
    DB 업데이트 없이, 크롤링 결과를 즉시 확인하는 디버그 엔드포인트.
    gzip 저장/상태전이 없이 '압축 전 JSON' 구조를 그대로 보여준다.
    """
    try:
        crawled = await vc.crawl_all_with_url(url)  # DB 사용 X
        posts = crawled.get("posts", [])

        # recent_activity 텍스트 만들기 (날짜|제목|본문 형태)
        lines = []
        for p in posts:
            d = normalize_created_at(p.get("published_at")) or ""
            t = p.get("title") or ""
            c = (p.get("text") or "").strip()
            # 불필요한 빈 줄 방지
            if d or t or c:
                lines.append(f"{d} | [{t}]\n{c}")
        recent_activity = "\n---\n".join(lines)

        recent_count = svc._count_recent_posts(posts, days=RECENT_WINDOW_DAYS, tz=LOCAL_TZ)
        recent_count = math.floor((recent_count+1)/2)
        if(len(posts) < recent_count):
            recent_count = len(posts)
        return {
            "source": "velog",
            "base_url": url,
            "post_count": int(crawled.get("post_count", len(posts))),
            "recent_count": recent_count,
            "recent_activity": recent_activity,
            "posts": posts,
        }

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail={"errorCode": "CRAWLING_FAILED", "message": str(e)},
        )