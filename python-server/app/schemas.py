from pydantic import BaseModel, Field
from typing import List

# === 기존 파일 업로드 관련 ===
class UploadResult(BaseModel):
    file_id: str = Field(..., example="123e4567-e89b-12d3-a456-426614174000")   # 저장 후 부여한 ID
    filename: str = Field(..., example="resume.pdf")                            # 원본 파일명
    size: int = Field(..., example=2048)                                        # 바이트 크기

# === NLP 요청 공통 ===
class NLPBaseRequest(BaseModel):
    type: str = Field(
        ...,
        description="문서 종류",
        example="resume",  # resume | portfolio | cover_letter
    )
    text: str = Field(
        ...,
        min_length=1,
        description="분석할 원문 텍스트",
        example="저는 전자공학을 전공하며 자율주행 프로젝트에 참여했습니다."
    )

# === 요약 API ===
class SummaryRequest(NLPBaseRequest):
    pass

class SummaryResponse(BaseModel):
    type: str = Field(..., example="resume")
    summary: str = Field(
        ...,
        example="전자공학 전공자로서 자율주행 프로젝트 경험이 있습니다."
    )

# === 키워드 API ===
class KeywordRequest(NLPBaseRequest):
    pass

class KeywordResponse(BaseModel):
    type: str = Field(..., example="portfolio")
    keywords: List[str] = Field(
        ...,
        example=["라즈베리파이", "YOLO", "자율주행", "임베디드", "MQTT"]
    )

# === 에러 응답 ===
class ErrorResponse(BaseModel):
    error: str = Field(..., example="INVALID_TYPE")
    message: str = Field(..., example="지원하지 않는 type 값입니다. (resume, portfolio, cover_letter 중 선택)")
