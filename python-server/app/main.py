from fastapi import FastAPI, Body
from config import settings
from app.routes import summary
import requests

app = FastAPI()

# 환경 변수 로드
API_KEY = settings.GEMINI_API_KEY.get_secret_value()


app.include_router(summary.router)

@app.get("/")
def root():
    return {"message": "서버가 정상적으로 작동되고 있습니다 🚀"}


@app.get("/connection-check")
def connection_check():
    if not API_KEY:
        return {"ok": False, "reason": "GEMINI_API_KEY missing from env"}

    url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key={API_KEY}"
    headers = {"Content-Type": "application/json"}

    payload = {
        "contents": [
            {
                "parts": [
                    {"text": "ping"}
                ]
            }
        ]
    }

    r = requests.post(url, headers=headers, json=payload, timeout=20)
    return {"status": r.status_code, "ok": r.ok, "raw": r.text}
