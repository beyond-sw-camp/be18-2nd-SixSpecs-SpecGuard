from fastapi import FastAPI
from dotenv import load_dotenv
import os, requests

app = FastAPI()

# 환경 변수 로드
load_dotenv()
API_KEY = os.getenv("GEMINI_API_KEY")

# 환경 변수 확인용 (디버깅)
@app.get("/env-check")
def env_check():
    return {"API_KEY": API_KEY}

# 외부(Gemini) 연결 상태 확인
@app.get("/connection-check")
def connection_check():
    if not API_KEY:
        return {"ok": False, "reason": "GEMINI_API_KEY missing from env"}

    url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key={API_KEY}"

    headers = {"Content-Type": "application/json"}

    payload = {
        "contents": [
            {
                "role": "user",
                "parts": [
                    {"text": "ping"}
                ]
            }
        ],
        "generationConfig": {
            "maxOutputTokens": 50,
            "temperature": 0.7,
        }
    }

    r = requests.post(url, headers=headers, json=payload, timeout=20)

    return {
        "status": r.status_code,
        "ok": r.ok,
        "raw": r.text,  # 구글 Gemini 응답 원문
    }
