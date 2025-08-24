import os
from typing import Optional
from fastapi import Header, HTTPException

# 안전 임포트
try:
    import jwt  # PyJWT
    from jwt import InvalidTokenError, ExpiredSignatureError
    _JWT_AVAILABLE = True
except Exception:
    jwt = None
    InvalidTokenError = ExpiredSignatureError = Exception
    _JWT_AVAILABLE = False

JWT_ALG = "HS256"
JWT_SECRET = os.environ.get("JWT_SECRET", "")
JWT_ISS = os.environ.get("JWT_ISS", "https://auth.specguard.com")
JWT_AUD = os.environ.get("JWT_AUD", "specguard-api")
ALLOW_ADMIN_TOKEN = os.environ.get("ALLOW_ADMIN_TOKEN", "false").lower() in {"1","true","yes"}
ADMIN_TOKEN = os.environ.get("ADMIN_TOKEN", "dev-admin-token")
JWT_LEEWAY = int(os.environ.get("JWT_LEEWAY_SEC", "0"))  # 선택: 클럭 드리프트 허용

def _raise(status:int, code:str, msg:str):
    raise HTTPException(status_code=status, detail={"error": code, "message": msg})

async def require_admin(authorization: Optional[str] = Header(None),
                        x_admin_token: Optional[str] = Header(None)):
    # 1) 테스트/운영 점검 우회
    if ALLOW_ADMIN_TOKEN and x_admin_token == ADMIN_TOKEN:
        return

    # 2) Bearer 필요
    if not authorization or not authorization.lower().startswith("bearer "):
        _raise(401, "UNAUTHORIZED", "액세스 토큰이 필요합니다")

    # 3) JWT 경로 준비 확인
    if not _JWT_AVAILABLE:
        _raise(500, "INTERNAL_SERVER_ERROR", "JWT 검증 모듈(PyJWT) 미설치")
    if not JWT_SECRET and JWT_ALG.startswith("HS"):  # HS256인 경우 필수
        _raise(500, "INTERNAL_SERVER_ERROR", "JWT_SECRET 미설정")

    token = authorization.split(" ", 1)[1].strip()
    try:
        decode_kwargs = {
            "algorithms": [JWT_ALG],
            "options": {"require": ["exp"]},
            "leeway": JWT_LEEWAY or 0,
        }
        # issuer/audience가 비어있지 않을 때만 검증에 사용
        if JWT_ISS:
            decode_kwargs["issuer"] = JWT_ISS
        if JWT_AUD:
            decode_kwargs["audience"] = JWT_AUD

        jwt.decode(token, JWT_SECRET, **decode_kwargs)
    except ExpiredSignatureError:
        _raise(401, "UNAUTHORIZED", "액세스 토큰이 만료되었습니다")
    except InvalidTokenError:
        _raise(401, "UNAUTHORIZED", "유효하지 않은 액세스 토큰입니다")
