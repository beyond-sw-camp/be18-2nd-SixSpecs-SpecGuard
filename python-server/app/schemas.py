from pydantic import BaseModel, Field

class UploadResult(BaseModel):
    file_id: str                              # 저장 후 부여한 ID
    filename: str                             # 원본 파일명
    size: int                                 # 바이트 크기

class TextSummaryRequest(BaseModel):
    text: str = Field(..., min_length=1, description="요약할 원문 텍스트")  # ... = 필수

class DocSummaryRequest(BaseModel):
    file_id: str = Field(..., description="업로드 후 받은 파일 ID")

class SummaryResponse(BaseModel):
    summary: str                              # 요약 결과

class ErrorResponse(BaseModel):
    detail: str                               # 에러 메시지 공통 포맷
