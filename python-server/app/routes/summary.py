from fastapi import APIRouter
from app.schemas import TextSummaryRequest, SummaryResponse
from app.services.gemini_client import client

router = APIRouter()

# client = JaeminaeClient()   # ✅ API 클라이언트 인스턴스 생성

@router.post("/summary", response_model=SummaryResponse)
async def summarize_text(request: TextSummaryRequest):
    """
    입력받은 텍스트를 간단히 요약해주는 API (임시 버전)
    """
    text = request.text.strip()

    prompt = f"다음 텍스트를 간단하게 요약해줘:\n\n{text}"

    response = client.models.generate_content(
        model='gemini-2.0-flash-001', contents=prompt
    )
    
    summary=response.text
    return SummaryResponse(summary=summary)
