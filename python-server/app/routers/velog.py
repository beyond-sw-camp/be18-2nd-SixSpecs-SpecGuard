from fastapi import APIRouter, HTTPException, Path, Body
from pydantic import BaseModel, Field

from app.services import crawler_service as svc

router = APIRouter(prefix="/api/v1", tags=["ingest"])

class StartBody(BaseModel):
    url: str = Field(..., description="Velog 프로필 URL (예: https://velog.io/@handle/posts)")

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