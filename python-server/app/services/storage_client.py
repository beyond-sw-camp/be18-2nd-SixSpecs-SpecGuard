import httpx
from typing import Any, Dict

async def send_to_storage(storage_url: str, payload: Dict[str, Any]) -> Dict[str, Any]:
    timeout = httpx.Timeout(60.0)
    async with httpx.AsyncClient(timeout=timeout) as client:
        try:
            resp = await client.post(storage_url, json=payload, headers={"Content-Type":"application/json"})
            # 2xx면 JSON 반환, 그 외도 본문을 그대로 전달
            if 200 <= resp.status_code < 300:
                try:
                    return resp.json()
                except Exception:
                    return {"status_code": resp.status_code, "text": resp.text}
            else:
                # 저장 서버 에러 내용을 보존해 상위로 전달
                try:
                    body = resp.json()
                except Exception:
                    body = {"text": resp.text}
                raise RuntimeError(f"{resp.status_code} from storage: {body}")
        except httpx.RequestError as e:
            # 연결 실패 등 네트워크 레벨 오류
            raise RuntimeError(f"connect error to storage: {e}") from e
