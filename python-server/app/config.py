# API 키, 환경 설정 로드
from pathlib import Path                     # 파일 경로 다룰 표준 라이브러리
from pydantic import SecretStr, AnyHttpUrl   # 비밀/URL 타입(검증 포함)
from pydantic_settings import BaseSettings, SettingsConfigDict  # .env 로드

class Settings(BaseSettings):
    JAEMINAE_API_KEY: SecretStr              # ★ 반드시 .env에서 받아야 하는 비밀 키
    JAEMINAE_BASE_URL: AnyHttpUrl = "https://api.jaeminae.ai/v1/summary"  # 기본 요약 API URL
    UPLOAD_DIR: Path = Path(__file__).resolve().parent / "uploads"         # 업로드 저장 폴더
    ENV: str = "dev"                         # 환경 표기(선택)

    # BaseSettings 동작 설정: .env 사용, 대소문자 구분 X
    model_config = SettingsConfigDict(env_file=".env", case_sensitive=False)

settings = Settings()                        # .env 읽어서 Settings 인스턴스 생성
settings.UPLOAD_DIR.mkdir(parents=True, exist_ok=True)  # 업로드 폴더 없으면 생성

