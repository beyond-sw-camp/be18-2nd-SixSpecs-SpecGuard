from typing import Optional
import re
from datetime import datetime, timedelta

def normalize_created_at(raw: Optional[str]) -> Optional[str]:
    if not raw:
        return None
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
