from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, declarative_base
from app.config import settings

# --- DB URL 구성 ---
# 예: mariadb+pymysql://user:password@host:port/dbname
DB_URL = (
    f"mariadb+pymysql://{settings.MARIADB_USER}:"
    f"{settings.MARIADB_PASSWORD}@"
    f"{settings.MARIADB_HOST}:"
    f"{settings.MARIADB_PORT}/"
    f"{settings.MARIADB_DB}"
)

# --- 엔진 생성 ---
engine = create_engine(DB_URL, echo=True, pool_pre_ping=True)

# --- 세션 팩토리 생성 ---
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# --- Base 클래스 (모든 모델이 이걸 상속받음) ---
Base = declarative_base()


# --- DB 세션 의존성 주입 함수 ---
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
