from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from app.services.gemini_client import client

router = APIRouter(prefix="/api/v1/nlp", tags=["summary"])

# === 요청/응답 스키마 ===
class SummaryRequest(BaseModel):
    type: str   # "resume" | "portfolio" | "cover_letter"
    text: str

class SummaryResponse(BaseModel):
    type: str
    summary: str


# === 요약 API ===
@router.post("/summary", response_model=SummaryResponse)
async def summarize_text(request: SummaryRequest):
    """
    입력받은 자소서 전문을 요약하는 API
    """

    # 1) type 검증
    if request.type not in ["cover_letter"]:
        raise HTTPException(
            status_code=400,
            detail={"error": "INVALID_TYPE",
                    "message": "지원하지 않는 type 값입니다. (cover_letter(자소서)만 요약합니다.)"}
        )

    # 2) text 검증
    if not request.text.strip():
        raise HTTPException(
            status_code=422,
            detail={"error": "EMPTY_TEXT",
                    "message": "요약할 텍스트가 비어있습니다."}
        )

    # 3) 프롬프트 생성
    prompt = f"다음 {request.type} 텍스트를 간단하게 요약해줘:\n\n{request.text.strip()}"

    # 4) Gemini API 호출
    try:
        response = client.models.generate_content(
            model="gemini-2.0-flash-001",
            contents=prompt
        )
        summary_text = response.text
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail={"error": "SUMMARY_FAILED",
                    "message": f"요약 생성에 실패했습니다. ({str(e)})"}
        )

    # 5) 결과 반환
    return SummaryResponse(type=request.type, summary=summary_text)
