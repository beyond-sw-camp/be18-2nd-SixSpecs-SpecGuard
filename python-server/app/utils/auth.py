import os
from typing import Optional
from fastapi import Header, HTTPException
import jwt
from jwt import InvalidTokenError, ExpiredSignatureError

JWT_ALG = "HS256"
JWT_SECRET = os.environ.get("JWT_SECRET", "")
JWT_ISS = os.environ.get("JWT_ISS", "https://auth.specguard.com")
JWT_AUD = os.environ.get("JWT_AUD", "specguard-api")
ALLOW_ADMIN_TOKEN = os.environ.get("ALLOW_ADMIN_TOKEN", "false").lower() in {"1","true","yes"}
ADMIN_TOKEN = os.environ.get("ADMIN_TOKEN", "dev-admin-token")

def _raise(status:int, code:str, msg:str):
    raise HTTPException(status_code=status, detail={"error": code, "message": msg})

async def require_admin(authorization: Optional[str] = Header(None),
                        x_admin_token: Optional[str] = Header(None)):
    if ALLOW_ADMIN_TOKEN and x_admin_token == ADMIN_TOKEN:
        return
    if not authorization or not authorization.lower().startswith("bearer "):
        _raise(401, "UNAUTHORIZED", "액세스 토큰이 필요합니다")
    if not JWT_SECRET:
        _raise(500, "INTERNAL_SERVER_ERROR", "JWT_SECRET 미설정")

    token = authorization.split(" ", 1)[1].strip()
    try:
        jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALG],
                   issuer=JWT_ISS or None, audience=JWT_AUD or None,
                   options={"require": ["exp"]})
    except ExpiredSignatureError:
        _raise(401, "UNAUTHORIZED", "액세스 토큰이 만료되었습니다")
    except InvalidTokenError:
        _raise(401, "UNAUTHORIZED", "유효하지 않은 액세스 토큰입니다")
