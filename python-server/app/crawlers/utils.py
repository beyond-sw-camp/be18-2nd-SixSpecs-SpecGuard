# 크롤링 유틸(선택 확장 지점).
from contextlib import contextmanager
from typing import Any
import time

@contextmanager
def with_context() -> Any:
    class _DummyCtx:
        def new_page(self):
            return None
    yield _DummyCtx()

def safe_sleep(sec: float):
    time.sleep(sec)

def dump_on_fail(page, name: str):
    try:
        page.screenshot(path=f"{name}.png", full_page=True)
    except Exception:
        pass
