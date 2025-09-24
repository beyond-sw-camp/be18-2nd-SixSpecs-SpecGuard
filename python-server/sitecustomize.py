import sys, asyncio

# Windows에선 Proactor가 서브프로세스 지원
if sys.platform.startswith("win"):
    try:
        asyncio.set_event_loop_policy(asyncio.WindowsProactorEventLoopPolicy())
    except Exception:
        # 안전 가드
        pass