import os
import json
import gzip
import asyncio
import httpx
from typing import Any, Dict, Optional, List

def _env_bool(key: str, default: str = "false") -> bool:
    return os.environ.get(key, default).lower() in {"1", "true", "yes"}

def _env_float(key: str, default: str) -> float:
    try:
        return float(os.environ.get(key, default))
    except Exception:
        return float(default)

def _auth_header() -> Dict[str, str]:
    token = os.environ.get("STORAGE_TOKEN", "").strip()
    return {"Authorization": f"Bearer {token}"} if token else {}

async def send_to_storage(
    storage_url: str,
    payload: List[Dict[str, Any]],
    *,
    use_gzip: Optional[bool] = None,
    timeout_sec: Optional[float] = None,
    retries: int = 2,
) -> Dict[str, Any] | str:
    """
    스토리지 서버로 링크/콘텐츠 배열을 POST 전송.
    - 기본은 평문 JSON. ENV STORAGE_USE_GZIP=true 이면 gzip 전송 시도.
    - 서버가 400/415 반환하면 gzip OFF로 폴백, 선형 백오프 재시도.
    """
    if use_gzip is None:
        use_gzip = _env_bool("STORAGE_USE_GZIP", "false")
    if timeout_sec is None:
        timeout_sec = _env_float("STORAGE_TIMEOUT_SEC", "60.0")

    base_headers = {"Accept": "application/json", **_auth_header()}

    async with httpx.AsyncClient(timeout=httpx.Timeout(timeout_sec)) as client:
        last_err: Optional[Exception] = None
        for attempt in range(retries + 1):
            try:
                if use_gzip:
                    raw = json.dumps(payload, ensure_ascii=False).encode("utf-8")
                    gz = gzip.compress(raw)
                    headers = {**base_headers, "Content-Type": "application/json", "Content-Encoding": "gzip"}
                    resp = await client.post(storage_url, content=gz, headers=headers)
                else:
                    headers = {**base_headers, "Content-Type": "application/json; charset=utf-8"}
                    resp = await client.post(storage_url, json=payload, headers=headers)

                resp.raise_for_status()
                try:
                    return resp.json()
                except Exception:
                    return resp.text  # JSON이 아니면 텍스트 그대로

            except httpx.HTTPStatusError as e:
                # gzip 미지원/형식 문제면 폴백
                if use_gzip and e.response is not None and e.response.status_code in (400, 415):
                    use_gzip = False
                    last_err = e
                    await asyncio.sleep(1.0 * (attempt + 1))
                    continue
                raise
            except Exception as e:
                last_err = e
                if attempt < retries:
                    await asyncio.sleep(1.0 * (attempt + 1))
                else:
                    break

        if last_err:
            raise last_err
        raise RuntimeError("Storage 전송 실패(원인 미상)")
