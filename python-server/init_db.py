import asyncio
from db import engine
from models import Base

async def init_models():
    async with engine.begin() as conn:
        print("✅ DB 연결 성공")
        await conn.run_sync(Base.metadata.create_all)
        print("✅ 테이블 생성 완료")

if __name__ == "__main__":
    asyncio.run(init_models())
