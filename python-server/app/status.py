# 서버 상태 확인하는 코드
from fastapi import APIRouter

router = APIRouter(tags=["status"])

@router.get("/status")
def status():
    return {"ok": True}                        # 200 OK면 로드밸런서/모니터링 통과
