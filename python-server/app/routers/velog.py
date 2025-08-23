import re
from urllib.parse import urlparse
from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, Query
from pydantic import BaseModel, Field

from app.utils.auth import require_admin
from app.services import crawler_service as svc

router = APIRouter(prefix="/api/v1/velog", tags=["velog"])

ALLOWED_HOSTS = {"velog.io", "www.velog.io"}
HANDLE_RE = re.compile(r"^[a-zA-Z0-9_]{1,30}$")
UUID36_RE = re.compile(r"^[0-9a-fA-F-]{36}$")

def _bad(status, code, msg):
    raise HTTPException(status_code=status, detail={"error": code, "message": msg})

def _assert_safe_handle(username: str):
    if not HANDLE_RE.match(username or ""):
        _bad(400, "INVALID_INPUT_VALUE", "Invalid handle")

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

def _assert_http_url(url: str, field_name: str):
    try:
        u = urlparse(url)
        if u.scheme not in {"http", "https"}:
            raise ValueError()
    except Exception:
        _bad(400, "INVALID_INPUT_VALUE", f"Invalid {field_name}")

class PostListItem(BaseModel):
    title: str
    url: str
    date: Optional[str] = None
    tags: List[str] = Field(default_factory=list)

class PostDetailReq(BaseModel):
    url: str

class CrawlForwardReq(BaseModel):
    username: str
    nlp_url: str
    max_posts: Optional[int] = 10

class CrawlAndStoreReq(BaseModel):
    username: str
    resume_id: str = Field(..., pattern=UUID36_RE.pattern)
    storage_url: str
    max_posts: Optional[int] = 10

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

@router.post("/post-detail", dependencies=[Depends(require_admin)])
async def get_post_detail(body: PostDetailReq):
    if not body.url:
        _bad(400, "INVALID_INPUT_VALUE", "Missing URL")
    _assert_safe_velog_url(body.url)
    data = await svc.post_detail(body.url)
    if not (data.get("title") or data.get("content")):
        _bad(404, "NOT_FOUND", "Post not found")
    return {"status": "success", "data": data}

@router.post("/crawl-and-forward", dependencies=[Depends(require_admin)])
async def crawl_and_forward(body: CrawlForwardReq):
    _assert_safe_handle(body.username)
    _assert_http_url(body.nlp_url, "nlp_url")
    count, nlp_resp = await svc.crawl_and_forward(body.username, body.nlp_url, body.max_posts)
    return {"status": "forwarded", "count": count, "nlp_response": nlp_resp}

@router.post("/crawl-and-store", dependencies=[Depends(require_admin)])
async def crawl_and_store(body: CrawlAndStoreReq):
    _assert_safe_handle(body.username)
    _assert_http_url(body.storage_url, "storage_url")
    if not UUID36_RE.match(body.resume_id or ""):
        _bad(400, "INVALID_INPUT_VALUE", "Invalid resume_id")
    result = await svc.crawl_and_store(body.username, body.resume_id, body.storage_url, body.max_posts)
    return {"status": "stored", **result}
