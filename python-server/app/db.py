import os
from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker, AsyncSession
from sqlalchemy import text
from dotenv import load_dotenv
load_dotenv()

DB_URL = os.getenv("DB_URL", "mysql+asyncmy://user:pass@localhost:3306/specguard")
engine = create_async_engine(DB_URL, pool_pre_ping=True, pool_recycle=1800)
SessionLocal = async_sessionmaker(engine, expire_on_commit=False, class_=AsyncSession)

# ---- 테이블/컬럼 매핑 (ENV로 덮어쓰기 가능) ----
# crawling_result
CR_TBL           = os.getenv("CRAWL_TABLE", "crawling_result")
CR_COL_ID        = os.getenv("CR_COL_ID", "id")
CR_COL_RID       = os.getenv("CRAWL_COL_RESUME_ID", "resume_id")
CR_COL_RLID      = os.getenv("CRAWL_COL_RESUME_LINK_ID", "resume_link_id")
CR_COL_STATUS    = os.getenv("CRAWL_COL_STATUS", "crawling_status")
CR_COL_CONTENTS  = os.getenv("CRAWL_COL_CONTENTS", "contents")

# resume_link
RL_TBL      = os.getenv("RESUME_LINK_TABLE", "resume_link")
RL_COL_ID   = os.getenv("RL_COL_ID", "id")
RL_COL_RID  = os.getenv("RL_COL_RESUME_ID", "resume_id")
RL_COL_URL  = os.getenv("RL_COL_URL", "url")
RL_COL_TYPE = os.getenv("RL_COL_LINK_TYPE", "link_type")
RL_TYPE_VELOG = os.getenv("RL_VELOG_TYPE", "VELOG")

# ---- 테이블/컬럼 매핑 (ENV로 덮어쓰기 가능) ----
# portfolio_result
PR_TBL                 = os.getenv("PORTFOLIO_TABLE", "portfolio_result")
PR_COL_ID              = os.getenv("PR_COL_ID", "id")
PR_COL_CRAWLING_RESULT = os.getenv("PR_COL_CRAWLING_RESULT_ID", "crawling_result_id")
PR_COL_PROCESSED       = os.getenv("PR_COL_PROCESSED_CONTENTS", "processed_contents")
PR_COL_CREATED_AT      = os.getenv("PR_COL_CREATED_AT", "created_at")
PR_COL_UPDATED_AT      = os.getenv("PR_COL_UPDATED_AT", "updated_at")
PR_COL_STATUS          = os.getenv("PR_COL_STATUS", "portfolio_status")

# --- resume_link에서 대상 링크 id 조회 ---
SQL_FIND_RESUME_LINK_ID = text(f"""
SELECT {RL_COL_ID} AS id
FROM {RL_TBL}
WHERE {RL_COL_RID} = :rid
  AND {RL_COL_TYPE} = :lt
  AND (
        {RL_COL_URL} = :url
        OR (:url = '' AND {RL_COL_URL} IS NULL)
      )
LIMIT 1
""")

# --- 상태 전이/저장 (crawling_result) ---

# PENDING -> RUNNING (CAS 선점)
SQL_CLAIM_RUNNING = text(f"""
UPDATE {CR_TBL}
SET {CR_COL_STATUS} = 'RUNNING', updated_at = CURRENT_TIMESTAMP
WHERE {CR_COL_RID}  = :rid
  AND {CR_COL_RLID} = :lid
  AND {CR_COL_STATUS} = 'PENDING'
""")

# URL 공란 -> NOTEXISTED (터미널 아니면)
SQL_SET_NOTEXISTED_IF_NOT_TERMINAL = text(f"""
UPDATE {CR_TBL}
SET {CR_COL_CONTENTS} = :contents,
    {CR_COL_STATUS}  = 'NOTEXISTED',
    updated_at       = CURRENT_TIMESTAMP
WHERE {CR_COL_RID}  = :rid
  AND {CR_COL_RLID} = :lid
  AND {CR_COL_STATUS} = 'PENDING'
""")

# RUNNING -> COMPLETED (성공 저장)
SQL_SAVE_COMPLETED = text(f"""
UPDATE {CR_TBL}
SET {CR_COL_CONTENTS} = :contents,
    {CR_COL_STATUS}  = 'COMPLETED',
    updated_at       = CURRENT_TIMESTAMP
WHERE {CR_COL_RID}  = :rid
  AND {CR_COL_RLID} = :lid
  AND {CR_COL_STATUS} = 'RUNNING'
""")

# RUNNING -> FAILED
SQL_SET_FAILED_IF_RUNNING = text(f"""
UPDATE {CR_TBL}
SET {CR_COL_STATUS} = 'FAILED',
    updated_at       = CURRENT_TIMESTAMP
WHERE {CR_COL_RID}  = :rid
  AND {CR_COL_RLID} = :lid
  AND {CR_COL_STATUS} = 'RUNNING'
""")

# --- portfolio_result 관련 SQL ---

SQL_FIND_CRAWLING_RESULTS_BY_RID = text(f"""
SELECT
    cr.{CR_COL_ID} AS crawling_result_id,
    cr.{CR_COL_RID} AS resume_id,
    cr.{CR_COL_RLID} AS resume_link_id,
    cr.{CR_COL_STATUS} AS crawling_status,
    cr.{CR_COL_CONTENTS} AS contents,
    rl.{RL_COL_TYPE} AS link_type
FROM {CR_TBL} AS cr
JOIN {RL_TBL} AS rl
    ON cr.{CR_COL_RLID} = rl.{RL_COL_ID}
WHERE cr.{CR_COL_RID} = :rid
""")

SQL_INSERT_PORTFOLIO_RESULT = text(f"""
INSERT INTO {PR_TBL} (
    {PR_COL_ID},
    {PR_COL_CRAWLING_RESULT},
    {PR_COL_PROCESSED},
    {PR_COL_CREATED_AT},
    {PR_COL_UPDATED_AT},
    {PR_COL_STATUS}
) VALUES (
    UUID(),
    :crawling_result_id,
    :processed_contents,
    NOW(),
    NOW(),
    :status
)
""")

SQL_UPDATE_PORTFOLIO_STATUS = text(f"""
UPDATE {PR_TBL}
SET {PR_COL_STATUS} = :status,
    {PR_COL_UPDATED_AT} = NOW()
WHERE {PR_COL_CRAWLING_RESULT} = :crawling_result_id
""")