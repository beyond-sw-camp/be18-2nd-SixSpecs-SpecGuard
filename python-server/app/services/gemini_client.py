# app/routes/resume.py
from google import genai
from dotenv import load_dotenv
import os
load_dotenv()

# 환경 변수 로드
API_KEY = os.getenv("GEMINI_API_KEY")

client = genai.Client(api_key=API_KEY)

