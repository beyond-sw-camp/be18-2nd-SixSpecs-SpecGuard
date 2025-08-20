# 자소서 관련 API
from fastapi import APIRouter, UploadFile, File, HTTPException, Depends
from app.schemas import UploadResult, DocSummaryRequest, SummaryResponse, ErrorResponse
from app.services.file_handler import save_upload, extract_text
from app.services.jaeminae_client import get_jaeminae_client, JaeminaeClient

router = APIRouter(prefix="/essay", tags=["essay"])

@router.post("/upload", response_model=UploadResult, responses={400: {"model": ErrorResponse}})
async def upload_essay(file: UploadFile = File(...)):
    content = await file.read()
    try:
        file_id, path, size = save_upload(file.filename, content)
    except ValueError as ve:
        raise HTTPException(status_code=400, detail=str(ve))
    return UploadResult(file_id=file_id, filename=file.filename, size=size)

@router.post("/summary", response_model=SummaryResponse, responses={400: {"model": ErrorResponse}})
async def summarize_essay(
    req: DocSummaryRequest,
    client: JaeminaeClient = Depends(get_jaeminae_client),
):
    from app.config import settings
    candidates = list(settings.UPLOAD_DIR.glob(req.file_id + ".*"))
    if not candidates:
        raise HTTPException(status_code=400, detail="file_id에 해당하는 파일을 찾을 수 없습니다.")
    path = candidates[0]

    text = extract_text(path)
    if not text.strip():
        raise HTTPException(status_code=400, detail="본문(텍스트)을 추출하지 못했습니다.")

    try:
        result = await client.summarize(text)
    except Exception as e:
        raise HTTPException(status_code=502, detail=f"요약 API 호출 실패: {e}")

    return SummaryResponse(summary=result.get("summary", ""))
