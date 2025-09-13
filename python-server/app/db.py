# app/db.py
import os
from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker, AsyncSession
from sqlalchemy import text

DB_URL = os.getenv("DB_URL", "mysql+asyncmy://user:pass@localhost:3306/specguard")
engine = create_async_engine(DB_URL, pool_pre_ping=True, pool_recycle=1800)
SessionLocal = async_sessionmaker(engine, expire_on_commit=False, class_=AsyncSession)

# ---- 테이블/컬럼 매핑 (ENV로 덮어쓰기 가능) ----
TBL = os.getenv("CRAWL_TABLE", "crawling_result")
COL_RESUME_ID = os.getenv("CRAWL_COL_RESUME_ID", "resume_id")
COL_URL       = os.getenv("CRAWL_COL_URL", "url")
COL_STATUS    = os.getenv("CRAWL_COL_STATUS", "crawling_status")
COL_CONTENTS_GZIP = os.getenv("CRAWL_COL_CONTENTS_GZIP", "contents_gzip")

# 1) 시작: PENDING -> RUNNING (선점)
SQL_CLAIM_RUNNING = text(f"""
UPDATE {TBL}
SET {COL_STATUS} = 'RUNNING', updated_at = CURRENT_TIMESTAMP
WHERE {COL_RESUME_ID} = :rid
  AND {COL_URL} = :url
  AND {COL_STATUS} = 'PENDING'
""")

# 2) URL 없음: NOTEXISTED + 더미 gzip (터미널이 아니면 업데이트)
SQL_SET_NOTEXISTED_IF_NOT_TERMINAL = text(f"""
UPDATE {TBL}
SET {COL_CONTENTS_GZIP} = :contents,
    {COL_STATUS} = 'NOTEXISTED',
    updated_at = CURRENT_TIMESTAMP
WHERE {COL_RESUME_ID} = :rid
  AND {COL_URL} = :url
  AND {COL_STATUS} NOT IN ('COMPLETED','FAILED','NOTEXISTED')
""")

# 3) 성공 저장: RUNNING -> COMPLETED
SQL_SAVE_COMPLETED = text(f"""
UPDATE {TBL}
SET {COL_CONTENTS_GZIP} = :contents,
    {COL_STATUS} = 'COMPLETED',
    updated_at = CURRENT_TIMESTAMP
WHERE {COL_RESUME_ID} = :rid
  AND {COL_URL} = :url
  AND {COL_STATUS} = 'RUNNING'
""")

# 4) 실패: RUNNING -> FAILED
SQL_SET_FAILED_IF_RUNNING = text(f"""
UPDATE {TBL}
SET {COL_STATUS} = 'FAILED', updated_at = CURRENT_TIMESTAMP
WHERE {COL_RESUME_ID} = :rid
  AND {COL_URL} = :url
  AND {COL_STATUS} = 'RUNNING'
""")
