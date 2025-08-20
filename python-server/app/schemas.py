from pydantic import BaseModel, Field

class UploadResult(BaseModel):
    file_id: str = Field(..., example="123e4567-e89b-12d3-a456-426614174000")   # 저장 후 부여한 ID
    filename: str = Field(..., example="resume.pdf")                            # 원본 파일명
    size: int = Field(..., example=2048)                                        # 바이트 크기

class TextSummaryRequest(BaseModel):
    text: str = Field(
        ...,
        min_length=1,
        description="요약할 원문 텍스트",
        example="저는 전자공학을 전공하며 자율주행 프로젝트에 참여했습니다."
    )

class DocSummaryRequest(BaseModel):
    file_id: str = Field(
        ...,
        description="업로드 후 받은 파일 ID",
        example="123e4567-e89b-12d3-a456-426614174000"
    )

class SummaryResponse(BaseModel):
    summary: str = Field(
        ...,
        example="전자공학 전공자로서 자율주행 프로젝트 경험이 있습니다."
    )

class ErrorResponse(BaseModel):
    detail: str = Field(
        ...,
        example="유효하지 않은 파일 ID 입니다."
    )
