import os
import asyncio
from typing import Optional, List, Dict, Any
from fastapi import HTTPException
from datetime import datetime, timedelta
from zoneinfo import ZoneInfo
from app.utils.dates import normalize_created_at

from app.crawlers import velog_crawler as vc
from app.utils.dates import normalize_created_at
from app.services.nlp_client import send_to_nlp


# ENV helpers
def _env_bool(key: str, default: str = "false") -> bool:
    return os.environ.get(key, default).lower() in {"1", "true", "yes"}

def _env_int(key: str, default: str) -> int:
    try:
        return int(os.environ.get(key, default))
    except Exception:
        return int(default)

def _env_float(key: str, default: str) -> float:
    try:
        return float(os.environ.get(key, default))
    except Exception:
        return float(default)


# Chunk helpers
def _chunked(seq: List[Any], size: int):
    """size 단위로 리스트를 잘라 제너레이터로 반환"""
    for i in range(0, len(seq), size):
        yield seq[i:i + size]

async def _sleep_ms(ms: int | float):
    if ms and ms > 0:
        await asyncio.sleep(float(ms) / 1000.0)


# 목록
async def list_posts(username: str, page: int, limit: int):
    links: List[str] = await vc.render_list_with_playwright(username)
    if not links:
        return []

    start, end = (page - 1) * limit, (page - 1) * limit + limit
    out = []
    for url in links[start:end]:
        title, _text, _langs, tags, published = await vc.render_post_with_playwright(url)
        out.append(
            {
                "title": title or "",
                "url": url,
                "date": normalize_created_at(published),
                "tags": tags or [],
            }
        )
    return out


# 상세
async def post_detail(url: str):
    title, text, langs, tags, published = await vc.render_post_with_playwright(url)

    # 크롤/파싱 실패로 판단(정책: 제목/본문 모두 없음)
    if not title and not text:
        raise HTTPException(
            status_code=500,
            detail={
                "error": "CRAWLING_FAILED",
                "message": "Velog 구조 변경 또는 예외로 인해 게시글을 파싱할 수 없습니다",
            },
        )

    MAX_TEXT_LEN = _env_int("MAX_TEXT_LEN", "200000")
    if text and len(text) > MAX_TEXT_LEN:
        text = text[:MAX_TEXT_LEN]

    return {
        "title": title,
        "url": url,
        "createdAt": normalize_created_at(published),
        "content": text or "",
        "tags": tags or [],
        "codeLangs": langs or [],
    }

async def crawl_and_forward(username: str, nlp_url: str, body_max_posts: Optional[int]):
    """
    NLP로 전송을 'post_count'와 'recent_activity'로 축소:
      - post_count: 전체 게시글 수 (링크 개수 기준)
      - recent_activity: 최근 1년 이내 게시글들의 '본문' 배열
    """
    # 1) 링크 수집 (전체 글 수 산정용)
    links: List[str] = await vc.render_list_with_playwright(username)
    post_count = len(links)

    if post_count == 0:
        raise HTTPException(
            status_code=500,
            detail={"error": "CRAWLING_FAILED", "message": "전달할 게시글이 없습니다. 사용자 또는 Velog 구조를 확인하세요"},
        )

    # 2) 최근 1년 윈도우
    window_days = _env_int("RECENT_WINDOW_DAYS", "365")
    cutoff = (datetime.now(ZoneInfo("Asia/Seoul")).date() - timedelta(days=window_days))

    recent_texts: List[str] = []

    # (옵션) 처리 상한: 너무 큰 계정 보호용
    scan_cap = body_max_posts or _env_int("DEFAULT_CRAWL_MAX_POSTS", "0")
    scanned = 0

    # 3) 상세 조회하며 최근 1년 본문만 수집
    for url in links:
        if scan_cap and scanned >= scan_cap:
            break
        scanned = 1
        try:
            title, text, _langs, _tags, published = await vc.render_post_with_playwright(url)
            iso = normalize_created_at(published)
            if not iso:
                continue
            try:
                y, m, d = map(int, iso.split("-"))
            except Exception:
                continue
            if datetime(y, m, d).date() >= cutoff:
                if text:
                    recent_texts.append(text)
            # (선택 최적화) 링크가 최신→과거 순이라면, 과거를 만나면 중단 가능
            # else:
            #     break
        except Exception:
            continue

    # 4) 축소 페이로드로 NLP 전송
    payload = {
        "source": "velog",
        "author": {"handle": username},
        "post_count": post_count,
        "recent_activity": recent_texts,   # 최근 1년 본문 배열
        "window_days": window_days,
        "schema_version": 2
    }
    resp = await send_to_nlp(nlp_url, payload)
    return post_count, resp