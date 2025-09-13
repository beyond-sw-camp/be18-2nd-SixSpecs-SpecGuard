import platform, sys
from fastapi import FastAPI, Request
from app.core.errors import install_error_handlers
from app.routers.velog import router as velog_router
from dotenv import load_dotenv; load_dotenv()


app = FastAPI(title="SpecGuard Velog API", version="1.3.0")

# 전역 에러 핸들러 설치
install_error_handlers(app)

# JSON UTF-8 강제
@app.middleware("http")
async def ensure_utf8_json(request: Request, call_next):
    resp = await call_next(request)
    ctype = resp.headers.get("content-type", "")
    if ctype.startswith("application/json") and "charset" not in ctype.lower():
        resp.headers["content-type"] = "application/json; charset=utf-8"
    return resp

# 라우터 등록
app.include_router(velog_router)

@app.get("/")
async def root():
    return {
        "status": "ok",
        "env": {"python": sys.version.split()[0], "os": platform.platform()},
        "note": "Use /api/v1/ingest/resumes/{resumeId}/velog/start",
    }
