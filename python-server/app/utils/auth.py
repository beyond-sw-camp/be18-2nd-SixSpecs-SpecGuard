import os
from typing import Optional
from fastapi import Header, HTTPException
import jwt
from jwt import InvalidTokenError, ExpiredSignatureError

JWT_ALG = "HS256"
JWT_SECRET = os.environ.get("JWT_SECRET", "")
JWT_ISS = os.environ.get("JWT_ISS", "https://auth.specguard.com")
JWT_AUD = os.environ.get("JWT_AUD", "specguard-api")

def _raise(status:int, code:str, msg:str):
    raise HTTPException(status_code=status, detail={"error": code, "message": msg})

# 테스트 할때는 아래의 주석처리된 require_admin() 사용
async def require_admin(authorization: Optional[str] = Header(None)):
    # 1) Bearer 필수
    if not authorization or not authorization.lower().startswith("bearer "):
        _raise(401, "UNAUTHORIZED", "액세스 토큰이 필요합니다")
    if not JWT_SECRET:
        _raise(500, "INTERNAL_SERVER_ERROR", "JWT_SECRET 미설정")

    # 2) JWT 검증
    token = authorization.split(" ", 1)[1].strip()
    try:
        claims = jwt.decode(
            token, JWT_SECRET, algorithms=[JWT_ALG],
            issuer=JWT_ISS or None, audience=JWT_AUD or None,
            options={"require": ["exp"]},
        )
    except ExpiredSignatureError:
        _raise(401, "UNAUTHORIZED", "액세스 토큰이 만료되었습니다")
    except InvalidTokenError:
        _raise(401, "UNAUTHORIZED", "유효하지 않은 액세스 토큰입니다")

    # 3) ADMIN 권한 확인 (role or roles/authorities)
    role  = claims.get("role")
    roles = claims.get("roles") or claims.get("authorities") or []
    if role != "ADMIN" and "ADMIN" not in roles:
        _raise(403, "ACCESS_DENIED", "관리자 권한이 필요합니다")