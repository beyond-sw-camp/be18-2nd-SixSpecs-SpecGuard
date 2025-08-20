# 이력서 관련 API
from fastapi import APIRouter, UploadFile, File, HTTPException, Depends
from app.schemas import UploadResult, DocSummaryRequest, SummaryResponse, ErrorResponse
from app.services.file_handler import save_upload, extract_text
from app.services.jaeminae_client import get_jaeminae_client, JaeminaeClient

router = APIRouter(prefix="/resume", tags=["resume"])           # /resume 네임스페이스

@router.post("/upload", response_model=UploadResult, responses={400: {"model": ErrorResponse}})
async def upload_resume(file: UploadFile = File(...)):          # 멀티파트 파일 받기
    content = await file.read()                                 # 파일 바이트 읽기
    try:
        file_id, path, size = save_upload(file.filename, content)  # 디스크 저장
    except ValueError as ve:
        raise HTTPException(status_code=400, detail=str(ve))    # 확장자 에러 등
    return UploadResult(file_id=file_id, filename=file.filename, size=size)

@router.post("/summary", response_model=SummaryResponse, responses={400: {"model": ErrorResponse}})
async def summarize_resume(
    req: DocSummaryRequest,                                     # {"file_id": "..."}
    client: JaeminaeClient = Depends(get_jaeminae_client),      # 의존성: 재민아이 클라이언트
):
    from app.config import settings
    # file_id.* 파일 찾기 (예: abc123.pdf)
    candidates = list(settings.UPLOAD_DIR.glob(req.file_id + ".*"))
    if not candidates:
        raise HTTPException(status_code=400, detail="file_id에 해당하는 파일을 찾을 수 없습니다.")
    path = candidates[0]

    text = extract_text(path)                                   # 텍스트 추출
    if not text.strip():
        raise HTTPException(status_code=400, detail="본문(텍스트)을 추출하지 못했습니다.")

    try:
        result = await client.summarize(text)                   # 재미나이 요약 호출
    except Exception as e:
        raise HTTPException(status_code=502, detail=f"요약 API 호출 실패: {e}")

    summary = result.get("summary") or ""                       # 응답에서 summary 필드
    return SummaryResponse(summary=summary)
