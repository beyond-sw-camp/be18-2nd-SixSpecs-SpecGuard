import os
from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker, AsyncSession
from sqlalchemy import text

DB_URL = os.getenv("DB_URL", "mysql+asyncmy://user:pass@localhost:3306/specguard")

engine = create_async_engine(DB_URL, pool_pre_ping=True, pool_recycle=1800)
SessionLocal = async_sessionmaker(engine, expire_on_commit=False, class_=AsyncSession)

# --- 상태 전이/저장 SQL ---

SQL_MARK_RESUME_CRAWLING = text("""
UPDATE resume
SET status = 'CRAWLING', updated_at = CURRENT_TIMESTAMP
WHERE id = :rid AND status = 'PENDING'
""")

SQL_GET_VELOG_LINKS = text("""
SELECT id, url, crawling_status
FROM resume_link
WHERE resume_id = :rid AND link_type = 'VELOG'
""")

# 링크 선점: PENDING일 때만 RUNNING으로
SQL_CLAIM_LINK_RUNNING = text("""
UPDATE resume_link
SET crawling_status = 'RUNNING', updated_at = CURRENT_TIMESTAMP
WHERE id = :lid AND crawling_status = 'PENDING'
""")

# URL 없음 → NONEXISTED (터미널 상태가 아니면)
SQL_SET_NONEXISTED_IF_NOT_TERMINAL = text("""
UPDATE resume_link
SET contents = :contents, crawling_status = 'NONEXISTED', updated_at = CURRENT_TIMESTAMP
WHERE id = :lid AND crawling_status NOT IN ('COMPLETED','FAILED','NONEXISTED')
""")

# 성공 저장: RUNNING일 때만 COMPLETED
SQL_SAVE_CONTENTS_COMPLETED = text("""
UPDATE resume_link
SET contents = :contents, crawling_status = 'COMPLETED', updated_at = CURRENT_TIMESTAMP
WHERE id = :lid AND crawling_status = 'RUNNING'
""")

# 실패: RUNNING → FAILED
SQL_SET_FAILED_IF_RUNNING = text("""
UPDATE resume_link
SET crawling_status = 'FAILED', updated_at = CURRENT_TIMESTAMP
WHERE id = :lid AND crawling_status = 'RUNNING'
""")

SQL_COUNT_PENDING_LINKS = text("""
SELECT COUNT(*) AS c
FROM resume_link
WHERE resume_id = :rid
AND crawling_status NOT IN ('COMPLETED','FAILED','NONEXISTED')
""")

# 모든 링크 종료 시, 이력서를 다음 단계로
SQL_MARK_RESUME_PROCESSING = text("""
UPDATE resume
SET status = 'PROCESSING', updated_at = CURRENT_TIMESTAMP
WHERE id = :rid AND status = 'CRAWLING'
""")

