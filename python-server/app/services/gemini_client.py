# app/routes/resume.py
from fastapi import APIRouter, HTTPException
from app.schemas import TextSummaryRequest, SummaryResponse, ErrorResponse
from app.services.jaeminae_client import JaeminaeClient   # ✅ 네 클라이언트 불러오기

router = APIRouter()
client = JaeminaeClient()   # ✅ API 클라이언트 인스턴스 생성

@router.post(
    "/resume-summary",
    response_model=SummaryResponse,
    responses={422: {"model": ErrorResponse}}
)
async def summarize_resume(request: TextSummaryRequest):
    if not request.text.strip():
        raise HTTPException(status_code=422, detail="빈 텍스트는 요약할 수 없습니다.")

    # ✅ 요약 API 호출
    summary_text = await client.summarize(request.text)

    return SummaryResponse(summary=summary_text)
