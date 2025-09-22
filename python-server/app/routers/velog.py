# from typing import Optional
# from fastapi import APIRouter, HTTPException, Path, Body, Query
# from pydantic import BaseModel, Field
# from app.services import crawler_service as svc

from typing import Optional
from fastapi import APIRouter, HTTPException, Path, Body, Query
from pydantic import BaseModel, Field
from app.services import crawler_service as svc
from app.crawlers import velog_crawler as vc       # ✅ 디버그용으로 직접 사용
from app.utils.dates import normalize_created_at 

router = APIRouter(prefix="/api/v1", tags=["ingest"])

@router.get("/debug/velog")
async def debug_velog(url: str = Query(..., description="Velog 프로필 URL (예: https://velog.io/@handle/posts)")):
    """
    DB 업데이트 없이, 크롤링 '생(raw)' 결과를 바로 확인하는 디버그 엔드포인트.
    """
    try:
        crawled = await vc.crawl_all_with_url(url)
        posts = crawled.get("posts", [])
        # recent_activity 형식
        lines = []
        for p in posts:
            d = normalize_created_at(p.get("published_at")) or ""
            t = p.get("title") or ""
            c = (p.get("text") or "").strip()
            lines.append(f"{d} | [{t}]\n{c}")
        recent_activity = "\n---\n".join(lines)

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