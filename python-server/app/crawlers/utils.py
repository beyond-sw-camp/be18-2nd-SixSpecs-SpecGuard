#크롤링 내구성/속도/디버깅을 위한 유틸.
#(예: Playwright 컨텍스트 생성, goto_with_retry, 실패 시 스냅샷 저장 등)
from contextlib import contextmanager
from typing import Any
from . import CONF

# 나중에 Playwright 연결로 바꿀 자리. 초기엔 더미 컨텍스트로 두면 서버는 뜸.
@contextmanager
def with_context() -> Any:
    class _DummyCtx:
        def new_page(self):  # 인터페이스만 흉내
            return None
    yield _DummyCtx()

def goto_with_retry(page, url: str, max_retry: int = 2):
    # 실제 구현 시: page.goto(url), networkidle 대기, 재시도 로직
    return

def dump_on_fail(page, name: str):
    # 실제 구현 시: 스냅샷/HTML 저장
    return
