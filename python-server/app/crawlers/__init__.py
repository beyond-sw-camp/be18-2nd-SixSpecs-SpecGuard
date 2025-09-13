import os

def _env_int(key: str, default: int) -> int:
    try:
        return int(os.getenv(key, str(default)))
    except Exception:
        return default

def _env_float(key: str, default: float) -> float:
    try:
        return float(os.getenv(key, str(default)))
    except Exception:
        return default

def _env_str(key: str, default: str) -> str:
    return os.getenv(key, default)

# UA는 운영에서 교체 가능하도록 ENV로 노출
UA = _env_str("CRAWLER_UA", "SpecGuardBot/1.0 (+https://example.com)")

CONF = {
    "headers": {"User-Agent": UA},
    "viewport": {
        "width": _env_int("CRAWLER_VIEWPORT_W", 1280),
        "height": _env_int("CRAWLER_VIEWPORT_H", 900),
    },
    "list": {
        "max_scrolls": _env_int("CRAWLER_MAX_SCROLLS", 150),
        # 고정 딜레이(폴백) + 지터 범위(설정 시 우선)
        "pause_sec": _env_float("CRAWLER_PAUSE_SEC", 1.0),
        "pause_sec_range": (
            _env_float("CRAWLER_PAUSE_MIN", 0.8),
            _env_float("CRAWLER_PAUSE_MAX", 1.6),
        ),
        "timeout_ms": _env_int("CRAWLER_LIST_TIMEOUT_MS", 25_000),
        "stagnant_rounds": _env_int("CRAWLER_STAGNANT_ROUNDS", 3),
    },
    "post": {
        "timeout_ms": _env_int("CRAWLER_POST_TIMEOUT_MS", 20_000),
        "hard_extra_sec": _env_int("CRAWLER_HARD_EXTRA_SEC", 4),
    },
}