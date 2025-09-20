import os, asyncio
from sqlalchemy.ext.asyncio import create_async_engine
from sqlalchemy import text
from dotenv import load_dotenv
from app.utils.codec import compress_gzip

load_dotenv()
url = os.environ.get("DB_URL")
if not url:
    raise SystemExit("DB_URL not set")

engine = create_async_engine(url, pool_pre_ping=True)

# 파일을 읽어서 bytes로 변환
# with open("github_stats.gz", "rb") as f:
#     file_data = f.read()

file_data = """
{
  "url": "https://www.notion.so/example",
  "title": "Notion 페이지 제목",
  "content": "본문 내용 독커, docker, 스프링 부트",
  "codeBlocks": ["System.out.println(\\"Hello\\");", "int a = 5;"],
  "tags": ["Java", "Notion"]
}
"""

file_data = compress_gzip(file_data)

async def main():
    async with engine.begin() as conn:
        r = await conn.execute(text("""
            INSERT INTO crawling_result (id, resume_id, resume_link_id, crawling_status, contents, created_at, updated_at)
            VALUES (:id, :resume_id, :resume_link_id, :status, :contents, :ca, :ua)
        """),
        {
            "id": "338b9e8b-7dc4-47a4-ab17-3e6abd9d5e7a",
            "resume_id": "dbf01b39-4c9c-470a-a516-4f613ed0e094",
            "resume_link_id": "573c5dfb-b54f-4726-9d02-62394a947941",
            "status": "COMPLETED",
            "contents": file_data,   # 바로 bytes 넣으면 LONGBLOB 저장됨,
            "ca": "2025-09-12 15:57:34.948006",
            "ua": "2025-09-12 15:57:34.948006"
        })

asyncio.run(main())




