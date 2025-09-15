from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import List
from app.services.gemini_client import client

router = APIRouter(prefix="/api/v1/nlp", tags=["keywords"])

# === 요청/응답 스키마 ===
class KeywordRequest(BaseModel):
    type: str   # "resume" | "portfolio" | "cover_letter"
    text: str

class KeywordResponse(BaseModel):
    type: str
    keywords: List[str]


# === 키워드 추출 API ===
@router.post("/keywords", response_model=KeywordResponse)
async def extract_keywords(request: KeywordRequest):
    """
    입력받은 이력서/포트폴리오/자소서 전문에서 핵심 키워드를 추출하는 API
    """

    # 1) type 검증
    if request.type not in ["resume", "portfolio", "cover_letter"]:
        raise HTTPException(
            status_code=400,
            detail={"error": "INVALID_TYPE",
                    "message": "지원하지 않는 type 값입니다. (resume, portfolio, cover_letter 중 선택)"}
        )

    # 2) text 검증
    if not request.text.strip():
        raise HTTPException(
            status_code=422,
            detail={"error": "EMPTY_TEXT",
                    "message": "키워드 추출할 텍스트가 비어있습니다."}
        )

    # 3) 프롬프트 생성
    prompt = f"""
    다음 {request.type} 텍스트에서 핵심 키워드 5개를 뽑아줘.
    - 출력은 JSON 배열 형식으로만 반환해.
    - 예시: ["AI", "자율주행", "IoT", "라즈베리파이", "MQTT"]

    텍스트: {request.text.strip()}
    """

    # 4) Gemini API 호출
    try:
        response = client.models.generate_content(
            model="gemini-2.0-flash-001",
            contents=prompt
        )

        # Gemini 응답에서 텍스트 추출
        raw_output = response.text.strip()

        # 안전하게 JSON 파싱 시도
        import json
        try:
            keywords = json.loads(raw_output)
        except json.JSONDecodeError:
            raise HTTPException(
                status_code=500,
                detail={"error": "INVALID_NLP_RESPONSE",
                        "message": f"NLP 서버 응답이 올바른 JSON 배열이 아닙니다: {raw_output}"}
            )

        if not isinstance(keywords, list):
            raise HTTPException(
                status_code=500,
                detail={"error": "INVALID_KEYWORD_FORMAT",
                        "message": "키워드 응답이 배열 형식이 아닙니다."}
            )

    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail={"error": "KEYWORD_EXTRACTION_FAILED",
                    "message": f"키워드 추출에 실패했습니다. ({str(e)})"}
        )

    # 5) 결과 반환
    return KeywordResponse(type=request.type, keywords=keywords)
