import sys
import asyncio
import uvicorn

def main():
    if sys.platform.startswith("win"):
        asyncio.set_event_loop_policy(asyncio.WindowsProactorEventLoopPolicy())
    uvicorn.run("app.main:app", host="127.0.0.1", port=8080, reload=False, log_level="info")

if __name__ == "__main__":
    main()
