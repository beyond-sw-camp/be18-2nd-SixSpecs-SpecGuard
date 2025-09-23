from typing import Optional
from fastapi import APIRouter, HTTPException, Path, Body, Query
from pydantic import BaseModel, Field
from app.services import crawler_service as svc
from app.crawlers import velog_crawler as vc      
from app.utils.dates import normalize_created_at 
from base64 import b64encode
import gzip, json

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
    


#압축 해제 확인
# app/routers/velog.py (예: 하단에 추가)
@router.get("/debug/resume-links/{resumeId}/velog/payload")
async def debug_get_payload(resumeId: str, url: str | None = Query(None)):
    # 1) 해당 resumeId(+url)로 링크 id 조회
    async with SessionLocal() as s:
        res = await s.execute(
            SQL_FIND_RESUME_LINK_ID,
            {"rid": resumeId, "lt": RL_TYPE_VELOG, "url": (url or "").strip()},
        )
        row = res.mappings().first()
    if not row:
        raise HTTPException(status_code=404, detail={"errorCode":"NOT_FOUND","message":"resume_link not found"})

    lid = row["id"]

    # 2) 저장된 contents(gzip) 가져오기
    async with SessionLocal() as s:
        res2 = await s.execute("SELECT contents FROM resume_link WHERE id=:lid", {"lid": lid})
        blob = res2.scalar()
    if not blob:
        raise HTTPException(status_code=404, detail={"errorCode":"NO_CONTENT","message":"no gzip contents stored"})

    # 3) gunzip → json 로드해서 그대로 반환
    try:
        payload = json.loads(gzip.decompress(blob).decode("utf-8"))
    except Exception as e:
        # 혹시 바이너리 확인이 필요하면 base64로도 확인 가능
        return {"error":"DECODE_FAILED","b64": b64encode(blob).decode("ascii"), "message": str(e)}

    return {"status":"ok", "payload": payload}
