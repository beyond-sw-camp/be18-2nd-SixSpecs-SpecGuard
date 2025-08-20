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

def _assert_safe_handle(username: str):
    if not HANDLE_RE.match(username or ""):
        raise HTTPException(status_code=400, detail="Invalid handle")

def _assert_safe_velog_url(url: str):
    try:
        u = urlparse(url)
    except Exception:
        raise HTTPException(status_code=400, detail="Invalid URL")
    if u.scheme not in {"http", "https"}:
        raise HTTPException(status_code=400, detail="Only http/https allowed")
    host = (u.hostname or "").lower()
    if host not in ALLOWED_HOSTS:
        raise HTTPException(status_code=400, detail="Only velog.io allowed")
    if u.port:
        raise HTTPException(status_code=400, detail="Custom port not allowed")

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

@router.get("/posts", dependencies=[Depends(require_admin)])
async def get_posts(
    username: str = Query(..., description="Velog 핸들(@ 제외)"),
    page: int = Query(1, ge=1),
    limit: int = Query(10, ge=1, le=100),
):
    _assert_safe_handle(username)
    items = await svc.list_posts(username, page, limit)
    if not items:
        raise HTTPException(status_code=404, detail="User not found or no posts")
    return {"status": "success", "data": items}

@router.post("/post-detail", dependencies=[Depends(require_admin)])
async def get_post_detail(body: PostDetailReq):
    if not body.url:
        raise HTTPException(status_code=400, detail="Missing URL")
    _assert_safe_velog_url(body.url)
    data = await svc.post_detail(body.url)
    if not (data.get("title") or data.get("content")):
        raise HTTPException(status_code=404, detail="Post not found")
    return {"status": "success", "data": data}

@router.post("/crawl-and-forward", dependencies=[Depends(require_admin)])
async def crawl_and_forward(body: CrawlForwardReq):
    _assert_safe_handle(body.username)
    count, nlp_resp = await svc.crawl_and_forward(body.username, body.nlp_url, body.max_posts)
    return {"status": "forwarded", "count": count, "nlp_response": nlp_resp}
