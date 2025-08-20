# 재미나이 API 호출 로직
import httpx
from app.config import settings

class JaeminaeClient:
    """
    재미나이 요약 API 비동기 클라이언트
    """
    def __init__(self):
        self._client = httpx.AsyncClient(                 # 비동기 HTTP 클라이언트
            base_url=str(settings.JAEMINAE_BASE_URL),     # 기본 URL 설정
            headers={
                "Authorization": f"Bearer {settings.JAEMINAE_API_KEY.get_secret_value()}",
                "Content-Type": "application/json",
            },
            timeout=30.0,                                 # 타임아웃
        )

    async def summarize(self, text: str) -> dict:
        payload = {"text": text}                          # ※ 실제 API 스펙에 맞게 필요 시 수정
        resp = await self._client.post("", json=payload)  # POST / (base_url 기준)
        resp.raise_for_status()                           # 4xx/5xx면 예외
        return resp.json()                                # JSON 본문 반환

    async def aclose(self):
        await self._client.aclose()                       # 리소스 정리

# FastAPI 의존성 주입용 제너레이터
async def get_jaeminae_client():
    client = JaeminaeClient()
    try:
        yield client                                      # 엔드포인트에서 client 사용
    finally:
        await client.aclose()                             # 요청 끝나면 닫기
