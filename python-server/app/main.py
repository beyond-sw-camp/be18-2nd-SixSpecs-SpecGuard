from fastapi import FastAPI, Body
from dotenv import load_dotenv
import os, requests

app = FastAPI()

# 환경 변수 로드
load_dotenv()
API_KEY = os.getenv("GEMINI_API_KEY")


@app.get("/")
def root():
    return {"message": "서버가 정상적으로 작동되고 있습니다 🚀"}


@app.get("/env-check")
def env_check():
    return {"API_KEY": API_KEY}


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


# 🎯 실제 요약 API
@app.post("/resume-summary")
def resume_summary(resume_text: str = Body(..., embed=True)):
    """
    클라이언트가 resume_text를 보내면 Gemini가 요약을 반환합니다.
    """
    if not API_KEY:
        return {"ok": False, "reason": "GEMINI_API_KEY missing from env"}

    url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key={API_KEY}"
    headers = {"Content-Type": "application/json"}

    prompt = f"다음 이력서를 300자로 요약해줘:\n\n{resume_text}"

    payload = {
        "contents": [
            {
                "parts": [
                    {"text": prompt}
                ]
            }
        ]
    }

    r = requests.post(url, headers=headers, json=payload, timeout=30)

    # Gemini 응답에서 요약 텍스트만 꺼내기
    try:
        data = r.json()
        summary = data["candidates"][0]["content"]["parts"][0]["text"]
    except Exception:
        summary = None

    return {
        "status": r.status_code,
        "ok": r.ok,
        "summary": summary,   # ✅ 요약 결과만 반환
        "raw": r.json()       # 참고용: 원문도 같이 돌려줌
    }
