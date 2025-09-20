from fastapi import HTTPException, APIRouter
from pydantic import BaseModel

from app.services.gemini_service import extract_keywrods_with_resume_id
from fastapi import  HTTPException, Body
from pydantic import BaseModel, Field

class StartBody(BaseModel):
    resumeId: str = Field(..., description="UUID resume.id")

router = APIRouter(prefix="/api/v1/nlp")

@router.post("/crawlingKeyword")
async def start_crawling_keywords(
    body: StartBody = Body(...),
):
    try:
        result = await extract_keywrods_with_resume_id(body.resumeId)
        return {"status": "success", "data": result}
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail={"errorCode":"INTERNAL_SERVER_ERROR", "message": str(e)})