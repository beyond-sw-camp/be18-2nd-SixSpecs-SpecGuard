import sys, asyncio

def ensure_proactor():
    if sys.platform.startswith("win"):
        asyncio.set_event_loop_policy(asyncio.WindowsProactorEventLoopPolicy())

def main():
    ensure_proactor()
    import uvicorn
    uvicorn.run("app.main:app", host="127.0.0.1", port=8000, reload=False, workers=1)

if __name__ == "__main__":
    main()