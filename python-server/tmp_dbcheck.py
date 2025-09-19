import os, asyncio
from sqlalchemy.ext.asyncio import create_async_engine
from sqlalchemy import text
from dotenv import load_dotenv

load_dotenv()
url = os.environ.get("DB_URL")
if not url:
    raise SystemExit("DB_URL not set")

engine = create_async_engine(url, pool_pre_ping=True)

async def main():
    async with engine.connect() as conn:
        r = await conn.execute(text("SELECT 1"))
        print("DB OK =>", r.scalar_one())

asyncio.run(main())