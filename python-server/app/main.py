#FastAPI 앱. 외부 요청을 받아 크롤러 모듈을 호출해서 JSON 응답 형식으로 리턴.
from fastapi import FastAPI, Query, HTTPException
from pydantic import BaseModel
from typing import List, Optional
from app.crawlers import velog_crawler as vc


# 모듈 임포트(순환에 강함)
from app.crawlers import velog_crawler as vc

app = FastAPI(title="SpecGuard Velog API", version="0.1.0")

# --- 공용 유틸(간단한 날짜 정규화만 유지) ---
def normalize_created_at(raw: Optional[str]) -> Optional[str]:
    if not raw:
        return None
    import re
    from datetime import datetime, timedelta
    s = raw.strip()
    m = re.search(r"(\d{4})[.\-]\s*(\d{1,2})[.\-]\s*(\d{1,2})", s)
    if m:
        y, mo, d = map(int, m.groups())
        return f"{y:04d}-{mo:02d}-{d:02d}"
    if "일 전" in s:
        n = int(re.search(r"(\d+)", s).group(1))
        return (datetime.now().date() - timedelta(days=n)).strftime("%Y-%m-%d")
    if "시간 전" in s or "분 전" in s:
        return datetime.now().date().strftime("%Y-%m-%d")
    return None

# --- 스키마 ---
class PostListItem(BaseModel):
    title: str
    url: str
    date: Optional[str] = None
    tags: List[str] = []

class PostDetailReq(BaseModel):
    url: str

@app.get("/")
def root():
    return {"status": "ok"}

# 목록 조회
@app.get("/api/v1/velog/posts")
def get_posts(
    username: str = Query(..., description="Velog 핸들(@ 제외)"),
    page: int = Query(1, ge=1),
    limit: int = Query(10, ge=1, le=100),
):
    if not username:
        raise HTTPException(status_code=400, detail="Missing username")

    links = vc.render_list_with_playwright(username)
    if not links:
        raise HTTPException(status_code=404, detail="User not found")

    start, end = (page - 1) * limit, (page - 1) * limit + limit
    items = []
    for url in links[start:end]:
        title, _text, _langs, tags, published = vc.render_post_with_playwright(url)
        items.append(
            PostListItem(
                title=title or "",
                url=url,
                date=normalize_created_at(published),
                tags=tags or [],
            )
        )
    return {"status": "success", "data": [i.dict() for i in items]}

# 상세 조회
@app.post("/api/v1/velog/post-detail")
def get_post_detail(body: PostDetailReq):
    if not body.url:
        raise HTTPException(status_code=400, detail="Missing URL")
    title, text, langs, tags, published = vc.render_post_with_playwright(body.url)
    if not (title or text):
        raise HTTPException(status_code=404, detail="Post not found")
    return {
        "status": "success",
        "data": {
            "title": title, "url": body.url,
            "createdAt": normalize_created_at(published),
            "content": text, "tags": tags, "codeLangs": langs,
        },
    }
