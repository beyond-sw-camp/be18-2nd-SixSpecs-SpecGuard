import re
from urllib.parse import urlparse
from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, Query
from pydantic import BaseModel, Field

from app.utils.auth import require_admin
from app.services import crawler_service as svc

#의존성/라우터 
router = APIRouter(prefix="/api/v1/velog", tags=["velog"])

#상수 정규식 
ALLOWED_HOSTS = {"velog.io", "www.velog.io"}
HANDLE_RE = re.compile(r"^[a-zA-Z0-9_]{1,30}$")
UUID36_RE = re.compile(r"^[0-9a-fA-F-]{36}$")

# 에러 헬퍼 
def _bad(status, code, msg):
    raise HTTPException(status_code=status, detail={"error": code, "message": msg})

#입력 검증 유틸
#핸들 형식 검증
def _assert_safe_handle(username: str):
    if not HANDLE_RE.match(username or ""):
        _bad(400, "INVALID_INPUT_VALUE", "Invalid handle")

#입력 검증 유틸
#벨로그 글 URL 검증
def _assert_safe_velog_url(url: str):
    try:
        u = urlparse(url)
    except Exception:
        _bad(400, "INVALID_INPUT_VALUE", "Invalid URL")
        return
    if u.scheme not in {"http", "https"}:
        _bad(400, "INVALID_INPUT_VALUE", "Only http/https allowed")
    host = (u.hostname or "").lower()
    if host not in ALLOWED_HOSTS:
        _bad(400, "INVALID_INPUT_VALUE", "Only velog.io allowed")
    if u.port:
        _bad(400, "INVALID_INPUT_VALUE", "Custom port not allowed")

#입력 검증 유틸
#일반 외부 URL 검증
def _assert_http_url(url: str, field_name: str):
    try:
        u = urlparse(url)
        if u.scheme not in {"http", "https"}:
            raise ValueError()
    except Exception:
        _bad(400, "INVALID_INPUT_VALUE", f"Invalid {field_name}")

#요청/응답 모델
class PostListItem(BaseModel):
    title: str
    url: str
    date: Optional[str] = None
    tags: List[str] = Field(default_factory=list)

#요청/응답 모델
class PostDetailReq(BaseModel):
    url: str

#요청/응답 모델
class CrawlForwardReq(BaseModel):
    username: str
    nlp_url: str
    max_posts: Optional[int] = None

#엔드 포인트
#게시글 목록 조회 
@router.get("/posts", dependencies=[Depends(require_admin)])
async def get_posts(
    username: str = Query(..., description="Velog 핸들(@ 제외)"),
    page: int = Query(1, ge=1),
    limit: int = Query(10, ge=1, le=100),
):
    _assert_safe_handle(username)
    items = await svc.list_posts(username, page, limit)
    if not items:
        _bad(404, "NOT_FOUND", "User not found or no posts")
    return {"status": "success", "data": items}

#게시글 상세 
@router.post("/post-detail", dependencies=[Depends(require_admin)])
async def get_post_detail(body: PostDetailReq):
    if not body.url:
        _bad(400, "INVALID_INPUT_VALUE", "Missing URL")
    _assert_safe_velog_url(body.url)
    data = await svc.post_detail(body.url)
    return {"status": "success", "data": data}

#전체 수집 후 NLP 전달 
@router.post("/crawl-and-forward", dependencies=[Depends(require_admin)])
async def crawl_and_forward(body: CrawlForwardReq):
    _assert_safe_handle(body.username)
    _assert_http_url(body.nlp_url, "nlp_url")
    count, nlp_resp = await svc.crawl_and_forward(body.username, body.nlp_url, body.max_posts)
    return {"status": "forwarded", "count": count, "nlp_response": nlp_resp}
