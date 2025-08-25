from __future__ import annotations
from typing import Optional
import re
from datetime import datetime, timedelta
from zoneinfo import ZoneInfo

# 절대 날짜 패턴
_ABS_PATTERNS = [
    re.compile(r'(?P<y>\d{4})[.\-/]\s*(?P<m>\d{1,2})[.\-/]\s*(?P<d>\d{1,2})'),
    re.compile(r'(?P<y>\d{4})\s*년\s*(?P<m>\d{1,2})\s*월\s*(?P<d>\d{1,2})\s*일?'),
]

# 상대 날짜 패턴
_DAYS_AGO    = re.compile(r'(\d+)\s*일\s*전')
_WEEKS_AGO   = re.compile(r'(\d+)\s*주\s*전')
_HOURS_AGO   = re.compile(r'(\d+)\s*시간\s*전')
_MINUTES_AGO = re.compile(r'(\d+)\s*분\s*전')

# 특수 키워드
_SPECIAL = {
    "어제": -1,
    "그제": -2,
    "오늘": 0,
    "방금": 0,
}

def normalize_created_at(
    raw: Optional[str],
    *,
    tz: Optional[str] = None,
    now: Optional[datetime] = None,
) -> Optional[str]:
    """
    Velog 등에서 가져온 게시 시각 문자열을 'YYYY-MM-DD'로 정규화.
    - tz: 기본 'Asia/Seoul'
    - now: 테스트 주입용 (미지정 시 현재 시각)
    """
    if not raw:
        return None
    s = raw.strip()
    # 1) 절대 날짜
    for pat in _ABS_PATTERNS:
        m = pat.search(s)
        if m:
            y, mo, d = int(m.group('y')), int(m.group('m')), int(m.group('d'))
            try:
                return f"{y:04d}-{mo:02d}-{d:02d}"
            except ValueError:
                return None

    zone = ZoneInfo(tz or "Asia/Seoul")
    base = now.astimezone(zone) if now else datetime.now(zone)

    # 2) 특수 단어
    for key, delta_days in _SPECIAL.items():
        if key in s:
            return (base.date() + timedelta(days=delta_days)).strftime("%Y-%m-%d")

    # 3) 상대 날짜(일/주)
    m = _DAYS_AGO.search(s)
    if m:
        days = int(m.group(1))
        return (base.date() - timedelta(days=days)).strftime("%Y-%m-%d")

    m = _WEEKS_AGO.search(s)
    if m:
        weeks = int(m.group(1))
        return (base.date() - timedelta(days=7 * weeks)).strftime("%Y-%m-%d")

    # 4) 상대 시간/분 -> 실제 시각에서 빼고 날짜 취득(자정 경계 보정)
    m = _HOURS_AGO.search(s)
    if m:
        hours = int(m.group(1))
        return (base - timedelta(hours=hours)).date().strftime("%Y-%m-%d")

    m = _MINUTES_AGO.search(s)
    if m:
        minutes = int(m.group(1))
        return (base - timedelta(minutes=minutes)).date().strftime("%Y-%m-%d")

    return None
