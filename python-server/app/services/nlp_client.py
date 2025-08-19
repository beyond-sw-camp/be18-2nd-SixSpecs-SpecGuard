# app/services/nlp_client.py
import os
import json
import gzip
import asyncio
import httpx
from typing import Any, Dict, Optional

def _env_bool(key: str, default: str = "false") -> bool:
    return os.environ.get(key, default).lower() in {"1", "true", "yes"}

def _env_float(key: str, default: str) -> float:
    try:
        return float(os.environ.get(key, default))
    except Exception:
        return float(default)

def _auth_header() -> Dict[str, str]:
    token = os.environ.get("NLP_TOKEN", "").strip()
    return {"Authorization": f"Bearer {token}"} if token else {}

async def send_to_nlp(
    nlp_url: str,
    payload: Dict[str, Any],
    *,
    use_gzip: Optional[bool] = None,
    timeout_sec: Optional[float] = None,
    retries: int = 3,
) -> Dict[str, Any]:
    """
    NLP 서버로 JSON(payload)을 POST 전송.
    - use_gzip: None이면 환경변수 NLP_USE_GZIP (기본 true) 사용
    - timeout_sec: 환경변수 NLP_TIMEOUT_SEC (기본 120.0) 사용
    - retries: 간단한 선형 backoff 재시도
    return: NLP 서버 JSON 응답(dict). JSON이 아니면 {"text": ...}
    """
    if use_gzip is None:
        use_gzip = _env_bool("NLP_USE_GZIP", "true")
    if timeout_sec is None:
        timeout_sec = _env_float("NLP_TIMEOUT_SEC", "120.0")

    base_headers = {
        "Accept": "application/json",
        **_auth_header(),
    }

    async with httpx.AsyncClient(timeout=httpx.Timeout(timeout_sec)) as client:
        last_err: Optional[Exception] = None
        for attempt in range(retries):
            try:
                if use_gzip:
                    raw = json.dumps(payload, ensure_ascii=False).encode("utf-8")
                    gz = gzip.compress(raw)
                    headers = {
                        **base_headers,
                        "Content-Type": "application/json",
                        "Content-Encoding": "gzip",
                    }
                    resp = await client.post(nlp_url, content=gz, headers=headers)
                else:
                    headers = {
                        **base_headers,
                        "Content-Type": "application/json; charset=utf-8",
                    }
                    resp = await client.post(nlp_url, json=payload, headers=headers)

                resp.raise_for_status()
                try:
                    return resp.json()
                except Exception:
                    return {"text": resp.text, "status": resp.status_code}

            except httpx.HTTPStatusError as e:
                # gzip 미지원/형식문제(400/415 등)면 gzip 비활성화 후 재시도
                if use_gzip and e.response is not None and e.response.status_code in (400, 415):
                    use_gzip = False
                    last_err = e
                    await asyncio.sleep(1.0 * (attempt + 1))
                    continue
                raise
            except Exception as e:
                last_err = e
                await asyncio.sleep(1.0 * (attempt + 1))

        # 모든 재시도 실패
        if last_err:
            raise last_err
        raise RuntimeError("NLP 전송 실패(원인 미상)")
