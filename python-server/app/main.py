import platform
from fastapi import FastAPI, Request
from app.core.errors import install_error_handlers
from app.routers.velog import router as velog_router
import sys, asyncio 
from dotenv import load_dotenv; load_dotenv()
from app.config import settings
from app.routes import summary, keywords   # ✅ keywords 라우터 추가
import requests

# 환경 변수 로드
API_KEY = settings.GEMINI_API_KEY.get_secret_value()

# 라우터 등록
app.include_router(summary.router)
app.include_router(keywords.router)

app = FastAPI(title="SpecGuard Python API", version="1.3.0")

# 전역 에러 핸들러 설치
install_error_handlers(app)

app.include_router(velog_router)

if sys.platform.startswith("win"):
    try:
        asyncio.set_event_loop_policy(asyncio.WindowsProactorEventLoopPolicy())
    except Exception:
        pass


# JSON UTF-8 강제
@app.middleware("http")
async def ensure_utf8_json(request: Request, call_next):
    resp = await call_next(request)
    ctype = resp.headers.get("content-type", "")
    if ctype.startswith("application/json") and "charset" not in ctype.lower():
        resp.headers["content-type"] = "application/json; charset=utf-8"
    return resp

# 라우터 등록

@app.get("/")
async def root():
    return {
        "status": "ok",
        "env": {"python": sys.version.split()[0], "os": platform.platform()},
        "note": """
        Use {
        /api/v1/ingest/resumes/{resumeId}/velog/start,
        /api/v1/nlp/summary,
        /api/v1/nlp/keywords
        }
        
        """,
    }
