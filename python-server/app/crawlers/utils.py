# 크롤링 유틸(선택 확장 지점).
import time

def safe_sleep(sec: float):
    time.sleep(sec)

def dump_on_fail(page, name: str):
    try:
        page.screenshot(path=f"{name}.png", full_page=True)
    except Exception:
        pass
