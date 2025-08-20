from fastapi import APIRouter
from app.schemas import TextSummaryRequest, SummaryResponse

router = APIRouter()

@router.post("/summary", response_model=SummaryResponse)
async def summarize_text(request: TextSummaryRequest):
    """
    입력받은 텍스트를 간단히 요약해주는 API (임시 버전)
    """
    text = request.text.strip()

    # 아주 단순한 요약 로직 (예시)
    # 실제론 Gemini 같은 모델 붙이면 됨
    if len(text) > 100:
        summary = text[:97] + "..."
    else:
        summary = text

    return SummaryResponse(summary=summary)
