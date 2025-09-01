# app/routes/resume.py
from google import genai
from app.config import settings

# 환경 변수 로드
API_KEY = settings.GEMINI_API_KEY.get_secret_value()

client = genai.Client(api_key=API_KEY)

