from fastapi import APIRouter, HTTPException
from app.schemas import KeywordRequest, BaseResponse
from app.services.gemini_service import extract_keywords
import json
import re

# === 라우터 설정 ===
router = APIRouter(prefix="/api/v1/nlp", tags=["keywords"])

@router.post("/keywords", response_model=BaseResponse)
async def extract_keywords_(request: KeywordRequest):
    """
    입력받은 포트폴리오/자소서 전문에서 핵심 키워드를 추출하는 API
    """

    # 1) type 검증
    if request.type not in ["cover_letter", "portfolio"]:
        raise HTTPException(
            status_code=400,
            detail={
                "error": "INVALID_TYPE",
                "message": "지원하지 않는 type 값입니다. (portfolio(포트폴리오), cover_letter(자소서) 중 선택)"
            }
        )

    # 2) text 검증
    if not request.text.strip():
        raise HTTPException(
            status_code=422,
            detail={
                "error": "EMPTY_TEXT",
                "message": "키워드 추출할 텍스트가 비어있습니다."
            }
        )

    # 3) 프롬프트 생성
    keywords = await extract_keywords(request.text)

    # 8) 최종 성공 응답
    return BaseResponse(status="sucesss", data={"keywords" : keywords})
