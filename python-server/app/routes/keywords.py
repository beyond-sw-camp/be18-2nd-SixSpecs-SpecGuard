from fastapi import APIRouter, HTTPException
from app.schemas import KeywordRequest, KeywordResponse
from app.services.gemini_client import client
import json
import re

# === 라우터 설정 ===
router = APIRouter(prefix="/api/v1/nlp", tags=["keywords"])

@router.post("/keywords", response_model=KeywordResponse)
async def extract_keywords(request: KeywordRequest):
    """
    입력받은 이력서/포트폴리오/자소서 전문에서 핵심 키워드를 추출하는 API
    """

    # 1) type 검증
    if request.type not in ["resume", "portfolio", "cover_letter"]:
        raise HTTPException(
            status_code=400,
            detail={
                "error": "INVALID_TYPE",
                "message": "지원하지 않는 type 값입니다. (resume, portfolio, cover_letter 중 선택)"
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
    prompt = f"""
    다음 {request.type} 텍스트에서 핵심 키워드 5개를 뽑아줘.
    - 출력은 JSON 배열 형식으로만 반환해.
    - 예시: ["AI", "자율주행", "IoT", "라즈베리파이", "MQTT"]
    - 코드 블록 표시(````json`, ```), 설명 문장, 줄바꿈 같은 건 절대 포함하지 마.
    텍스트: {request.text.strip()}
    """

    try:
        # 4) Gemini API 호출
        response = client.models.generate_content(
            model="gemini-2.0-flash-001",
            contents=prompt
        )
        raw_output = response.text.strip()

        # 5) 전처리: 코드블록 제거
        clean_output = re.sub(r"```(?:json)?", "", raw_output)
        clean_output = clean_output.replace("```", "").strip()

        # 6) JSON 배열 파싱
        try:
            keywords = json.loads(clean_output)
        except json.JSONDecodeError:
            raise HTTPException(
                status_code=500,
                detail={
                    "error": "INVALID_NLP_RESPONSE",
                    "message": f"NLP 서버 응답이 올바른 JSON 배열이 아닙니다: {raw_output}"
                }
            )

        # 7) 결과 타입 검증
        if not isinstance(keywords, list):
            raise HTTPException(
                status_code=500,
                detail={
                    "error": "INVALID_KEYWORD_FORMAT",
                    "message": "키워드 응답이 배열 형식이 아닙니다."
                }
            )

    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail={
                "error": "KEYWORD_EXTRACTION_FAILED",
                "message": f"키워드 추출에 실패했습니다. ({str(e)})"
            }
        )

    # 8) 최종 성공 응답
    return KeywordResponse(type=request.type, keywords=keywords)
