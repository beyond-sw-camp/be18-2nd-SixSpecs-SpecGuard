# 파일 저장/읽기 기능
import uuid
from pathlib import Path
from typing import Tuple
from app.config import settings

ALLOWED_EXT: tuple[str, ...] = (".pdf", ".docx", ".txt")       # 허용 확장자

def _gen_id() -> str:
    return uuid.uuid4().hex                                     # 32자리 고유 ID

def save_upload(filename: str, content: bytes) -> tuple[str, Path, int]:
    """
    업로드 파일을 디스크에 저장하고 (file_id, 경로, 크기) 반환
    """
    ext = Path(filename).suffix.lower()                         # 확장자 소문자
    if ext not in ALLOWED_EXT:
        raise ValueError(f"지원하지 않는 확장자입니다: {ext} (허용: {ALLOWED_EXT})")

    file_id = _gen_id()                                         # 새 ID
    dest = settings.UPLOAD_DIR / f"{file_id}{ext}"              # 저장 경로
    dest.write_bytes(content)                                   # 바이트 저장
    return file_id, dest, len(content)

def extract_text(file_path: Path) -> str:
    """
    파일에서 텍스트 추출
    - .txt  : 그대로 읽기
    - .docx : python-docx
    - .pdf  : pdfplumber
    """
    ext = file_path.suffix.lower()

    if ext == ".txt":
        return file_path.read_text(encoding="utf-8", errors="ignore")

    if ext == ".docx":
        try:
            from docx import Document                          # 필요 시 설치
        except Exception as e:
            raise RuntimeError("DOCX 처리를 위해 'python-docx'가 필요합니다. `poetry add python-docx`") from e
        doc = Document(str(file_path))
        return "\n".join(p.text for p in doc.paragraphs).strip()

    if ext == ".pdf":
        try:
            import pdfplumber                                  # 필요 시 설치
        except Exception as e:
            raise RuntimeError("PDF 처리를 위해 'pdfplumber'가 필요합니다. `poetry add pdfplumber`") from e
        chunks: list[str] = []
        with pdfplumber.open(str(file_path)) as pdf:
            for page in pdf.pages:
                chunks.append(page.extract_text() or "")
        return "\n".join(chunks).strip()

    # 여긴 오지 않음 (이미 확장자 검증)
    raise ValueError(f"처리할 수 없는 확장자: {ext}")
