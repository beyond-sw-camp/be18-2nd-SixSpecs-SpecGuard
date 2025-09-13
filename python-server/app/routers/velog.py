import typing as t
from fastapi import APIRouter, Depends, HTTPException, Path, Body
from pydantic import BaseModel

from app.utils.auth import require_internal
from app.services import crawler_service as svc

router = APIRouter(prefix="/api/v1", tags=["ingest"])

class StartBody(BaseModel):
    # 스키마상 URL은 resume_link.url에서 읽으므로 옵션
    url: t.Optional[str] = None

@router.post("/ingest/resumes/{resumeId}/velog/start", dependencies=[Depends(require_internal)])
async def start_velog_ingest(
    resumeId: str = Path(..., description="resume.id (UUID)"),
    body: StartBody = Body(default=StartBody())
):
    try:
        result = await svc.ingest_velog_for_resume(resumeId, body.url)
        return {"status": "success", "data": result}
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail={"error":"INTERNAL_SERVER_ERROR", "message": str(e)})
