from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column
from sqlalchemy import String, Text, Date, DateTime, Enum, Boolean, Float, JSON, Integer
from datetime import datetime, date


# --- Base 클래스 (모든 ORM 모델이 상속받음) ---
class Base(DeclarativeBase):
    pass


# ========================
# 회사가 만든 채용 템플릿 필드
# ========================
class CompanyTemplateField(Base):
    __tablename__ = "company_template_field"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    template_id: Mapped[str] = mapped_column(String(36), nullable=False)  # 템플릿 PK
    field_name: Mapped[str] = mapped_column(String(100), nullable=False)  # 필드 이름
    field_type: Mapped[str] = mapped_column(String(50), nullable=False)   # 필드 타입 (ENUM 대신 문자열로)
    is_required: Mapped[bool] = mapped_column(Boolean, default=False)     # 필수 여부
    field_order: Mapped[int] = mapped_column(Integer)                     # 출력 순서
    options: Mapped[dict] = mapped_column(JSON)                           # 선택지 (라디오, 드롭다운 등)
    min_length: Mapped[int] = mapped_column(Integer, default=0)           # 최소 길이
    max_length: Mapped[int] = mapped_column(Integer, default=500)         # 최대 길이
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


# ========================
# 이력서 기본 정보
# ========================
class ResumeBasic(Base):
    __tablename__ = "resume_basic"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)             # UUID
    resume_id: Mapped[str] = mapped_column(String(36), nullable=False)        # 이력서 ID
    english_name: Mapped[str] = mapped_column(String(100), nullable=False)    # 영어 이름
    gender: Mapped[str] = mapped_column(String(10), nullable=False)           # 성별
    birth_date: Mapped[date] = mapped_column(Date, nullable=False)            # 생년월일
    nationality: Mapped[str] = mapped_column(String(50), nullable=False)      # 국적
    address: Mapped[str] = mapped_column(String(255), nullable=False)         # 주소
    specialty: Mapped[str] = mapped_column(String(255))                       # 특기
    hobbies: Mapped[str] = mapped_column(String(255))                         # 취미
    profile_image_url: Mapped[str] = mapped_column(String(512), nullable=False) # 프로필 이미지 URL
    apply_field: Mapped[str] = mapped_column(String(100), nullable=False)     # 지원 분야
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


# ========================
# 서비스 사용 로그
# ========================
class UsageLog(Base):
    __tablename__ = "usage_log"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    company_id: Mapped[str] = mapped_column(String(36), nullable=False)   # 회사 ID
    event_type: Mapped[str] = mapped_column(String(50), nullable=False)   # 이벤트 타입
    resume_count: Mapped[int] = mapped_column(Integer, default=1)         # 사용된 이력서 개수
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)


# ========================
# 이력서 메인 테이블
# ========================
class Resume(Base):
    __tablename__ = "resume"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)             # Resume PK
    template_id: Mapped[str] = mapped_column(String(36), nullable=False)      # 템플릿 ID
    status: Mapped[str] = mapped_column(String(20), default="DRAFT")          # 상태 (DRAFT, SUBMITTED 등)
    name: Mapped[str] = mapped_column(String(50), nullable=False)             # 이름
    phone: Mapped[str] = mapped_column(String(50), nullable=False)            # 연락처
    email: Mapped[str] = mapped_column(String(255), nullable=False)           # 이메일
    password_hash: Mapped[str] = mapped_column(String(64), nullable=False)    # 패스워드 해시
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


# ========================
# 내부 관리자 계정
# ========================
class InternalAdmin(Base):
    __tablename__ = "internal_admin"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    name: Mapped[str] = mapped_column(String(100), nullable=False)            # 이름
    email: Mapped[str] = mapped_column(String(255), nullable=False)           # 이메일
    password_hash: Mapped[str] = mapped_column(String(64), nullable=False)    # 패스워드 해시
    role: Mapped[str] = mapped_column(String(50), default="ADMIN")            # 관리자 권한
    phone: Mapped[str] = mapped_column(String(50))                            # 전화번호
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)


# ========================
# 요금제 (Plan)
# ========================
class Plan(Base):
    __tablename__ = "plan"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)             # Plan ID
    name: Mapped[str] = mapped_column(String(50), nullable=False)             # 요금제 이름
    price: Mapped[int] = mapped_column(Integer, nullable=False)               # 요금
    currency: Mapped[str] = mapped_column(String(10), nullable=False)         # 화폐 단위
    billing_cycle: Mapped[str] = mapped_column(String(10), nullable=False)    # 결제 주기
    resume_limit: Mapped[int] = mapped_column(Integer, nullable=False)        # 제출 이력서 제한
    analysis_limit: Mapped[int] = mapped_column(Integer, nullable=False)      # 분석 횟수 제한
    user_limit: Mapped[int] = mapped_column(Integer, nullable=False)          # 사용자 제한
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)

# ========================
# 이력서에 연결된 외부 링크 (Github, Notion, Velog 등)
# ========================
class ResumeLink(Base):
    __tablename__ = "resume_link"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    resume_id: Mapped[str] = mapped_column(String(36), nullable=False)        # 이력서 ID
    url: Mapped[str] = mapped_column(Text)                                    # URL
    link_type: Mapped[str] = mapped_column(String(50), nullable=False)        # 링크 타입 (Github, Notion, Velog 등)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


# ========================
# 기업 이메일 인증 상태
# ========================
class CompanyEmailVerifyStatus(Base):
    __tablename__ = "company_email_verify_status"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    company_id: Mapped[str] = mapped_column(String(36))
    email: Mapped[str] = mapped_column(String(255), nullable=False)           # 기업 이메일
    status: Mapped[str] = mapped_column(String(20), default="PENDING")        # 인증 상태
    last_request_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    verified_at: Mapped[datetime] = mapped_column(DateTime)
    attempts: Mapped[int] = mapped_column(Integer, default=0)                 # 시도 횟수
    last_ip: Mapped[str] = mapped_column(String(45))                          # 최근 접속 IP
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


# ========================
# 평가 가중치 (특정 평가 항목에 대한 비중)
# ========================
class EvaluationWeight(Base):
    __tablename__ = "evaluation_weight"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    evaluation_profile_id: Mapped[str] = mapped_column(String(36), nullable=False)  # 평가 프로필 ID
    weight_type: Mapped[str] = mapped_column(String(50), nullable=False)            # 가중치 타입
    weight_value: Mapped[float] = mapped_column(Float, nullable=False)              # 가중치 값
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

# ========================
# 이력서 - 경력 정보
# ========================
class ResumeExperience(Base):
    __tablename__ = "resume_experience"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    resume_id: Mapped[str] = mapped_column(String(36), nullable=False)        # 이력서 ID
    company_name: Mapped[str] = mapped_column(String(255), nullable=False)    # 회사명
    department: Mapped[str] = mapped_column(String(255), nullable=False)      # 부서
    position: Mapped[str] = mapped_column(String(255), nullable=False)        # 직급
    responsibilities: Mapped[str] = mapped_column(String(255))                # 담당 업무
    start_date: Mapped[date] = mapped_column(Date, nullable=False)            # 입사일
    end_date: Mapped[date] = mapped_column(Date)                              # 퇴사일
    employment_status: Mapped[str] = mapped_column(String(20), nullable=False) # 고용 상태
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


# ========================
# 이력서 - 학력 정보
# ========================
class ResumeEducation(Base):
    __tablename__ = "resume_education"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    resume_id: Mapped[str] = mapped_column(String(36), nullable=False)        # 이력서 ID
    school_name: Mapped[str] = mapped_column(String(255), nullable=False)     # 학교명
    major: Mapped[str] = mapped_column(String(255))                           # 전공
    graduation_status: Mapped[str] = mapped_column(String(50), nullable=False) # 졸업 상태
    degree: Mapped[str] = mapped_column(String(50), nullable=False)           # 학위
    admission_type: Mapped[str] = mapped_column(String(50), nullable=False)   # 입학 유형
    gpa: Mapped[float] = mapped_column(Float, nullable=False)                 # 학점
    max_gpa: Mapped[float] = mapped_column(Float, nullable=False)             # 최대 학점
    start_date: Mapped[date] = mapped_column(Date, nullable=False)            # 입학일
    end_date: Mapped[date] = mapped_column(Date)                              # 졸업일
    school_type: Mapped[str] = mapped_column(String(50), nullable=False)      # 학교 구분
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


# ========================
# 클라이언트 사용자 (기업 관리자/뷰어 등)
# ========================
class ClientUser(Base):
    __tablename__ = "client_user"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    company_id: Mapped[str] = mapped_column(String(36), nullable=False)       # 회사 ID
    name: Mapped[str] = mapped_column(String(100), nullable=False)            # 이름
    email: Mapped[str] = mapped_column(String(255), nullable=False)           # 이메일
    password_hash: Mapped[str] = mapped_column(String(64), nullable=False)    # 패스워드 해시
    role: Mapped[str] = mapped_column(String(50), default="VIEWER")           # 권한 (OWNER, MANAGER, VIEWER)
    phone: Mapped[str] = mapped_column(String(50))                            # 전화번호
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    provider: Mapped[str] = mapped_column(String(20), default="local")        # 소셜 로그인 제공자
    provider_id: Mapped[str] = mapped_column(String(100))                     # 소셜 로그인 ID
    profile_image: Mapped[str] = mapped_column(String(500))                   # 소셜 프로필 이미지


# ========================
# 클라이언트 회사
# ========================
class ClientCompany(Base):
    __tablename__ = "client_company"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)             # 회사 ID
    name: Mapped[str] = mapped_column(String(50), nullable=False)             # 회사명
    business_number: Mapped[str] = mapped_column(String(12), nullable=False)  # 사업자 번호
    slug: Mapped[str] = mapped_column(String(64))                             # 슬러그
    manager_position: Mapped[str] = mapped_column(String(64), nullable=False) # 담당자 직책
    manager_name: Mapped[str] = mapped_column(String(30), nullable=False)     # 담당자 이름
    contact_mobile: Mapped[str] = mapped_column(String(100), nullable=False)  # 연락처
    contact_email: Mapped[str] = mapped_column(String(100), nullable=False)   # 이메일
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)

# ========================
# 이력서 - 자격증 정보
# ========================
class ResumeCertificate(Base):
    __tablename__ = "resume_certificate"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    resume_id: Mapped[str] = mapped_column(String(36), nullable=False)        # 이력서 ID
    certificate_name: Mapped[str] = mapped_column(String(255), nullable=False) # 자격증 이름
    certificate_number: Mapped[str] = mapped_column(String(255), nullable=False) # 발급 번호
    issuer: Mapped[str] = mapped_column(String(255), nullable=False)          # 발급 기관
    issued_date: Mapped[date] = mapped_column(Date, nullable=False)           # 취득일
    cert_url: Mapped[str] = mapped_column(Text)                               # 자격증 URL
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


# ========================
# 정합성 분석 로그
# ========================
class ValidationResultLog(Base):
    __tablename__ = "validation_result_log"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    validation_result_id: Mapped[str] = mapped_column(String(36), nullable=False) # 분석 결과 ID
    validation_score: Mapped[float] = mapped_column(Float, nullable=False)        # 정합성 점수
    summary: Mapped[str] = mapped_column(Text)                                    # 요약
    keyword_list: Mapped[str] = mapped_column(Text)                               # 키워드 목록
    mismatch_fields: Mapped[dict] = mapped_column(JSON)                           # 불일치 필드
    validated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow) # 분석 시각
    description_comment: Mapped[str] = mapped_column(Text)                        # 분석 코멘트


# ========================
# 크롤링 결과 저장
# ========================
class CrawlingResult(Base):
    __tablename__ = "crawling_result"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    resume_id: Mapped[str] = mapped_column(String(36), nullable=False)            # 이력서 ID
    resume_link_id: Mapped[str] = mapped_column(String(36), nullable=False)       # 링크 ID
    crawling_status: Mapped[str] = mapped_column(String(20), default="PENDING")   # 크롤링 상태
    contents: Mapped[bytes] = mapped_column(Text)                                 # 크롤링 결과 (텍스트/바이너리)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


# ========================
# 지원자 이메일 인증 상태
# ========================
class ApplicantEmailVerifyStatus(Base):
    __tablename__ = "applicant_email_verify_status"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    resume_id: Mapped[str] = mapped_column(String(36), nullable=False)            # 이력서 ID
    email: Mapped[str] = mapped_column(String(255), nullable=False)               # 지원자 이메일
    status: Mapped[str] = mapped_column(String(20), default="PENDING")            # 인증 상태
    last_request_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    verified_at: Mapped[datetime] = mapped_column(DateTime)
    attempts: Mapped[int] = mapped_column(Integer, default=0)                     # 시도 횟수
    last_ip: Mapped[str] = mapped_column(String(45))                              # 최근 접속 IP
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


# ========================
# Github 메타데이터
# ========================
class GithubMetadata(Base):
    __tablename__ = "github_metatdata"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    resume_link_id: Mapped[str] = mapped_column(String(36), nullable=False)       # 링크 ID
    repo_count: Mapped[int] = mapped_column(Integer, nullable=False)              # 레포 개수
    commits: Mapped[int] = mapped_column(Integer)                                 # 커밋 수
    main_language: Mapped[str] = mapped_column(String(50))                        # 주 언어
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)


# ========================
# 평가 프로필 (기업에서 정의한 평가 기준)
# ========================
class EvaluationProfile(Base):
    __tablename__ = "evaluation_profile"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    company_id: Mapped[str] = mapped_column(String(36), nullable=False)           # 회사 ID
    name: Mapped[str] = mapped_column(String(255), nullable=False)                # 프로필 이름
    description: Mapped[str] = mapped_column(Text)                                # 설명
    is_active: Mapped[bool] = mapped_column(Boolean, nullable=False, default=True) # 활성화 여부
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


# ========================
# 정합성 이슈 (검증 에러 유형)
# ========================
class ValidationIssue(Base):
    __tablename__ = "validation_issue"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    issue_type: Mapped[str] = mapped_column(String(50), nullable=False)           # 이슈 타입
    issue_description: Mapped[str] = mapped_column(Text)                          # 상세 설명
    severity: Mapped[str] = mapped_column(String(20), nullable=False)             # 심각도 (LOW, MEDIUM, HIGH)

# ========================
# 회사 템플릿 응답 (지원자가 작성한 답변)
# ========================
class CompanyTemplateResponse(Base):
    __tablename__ = "company_template_response"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    field_id: Mapped[str] = mapped_column(String(36), nullable=False)          # 템플릿 필드 ID
    resume_id: Mapped[str] = mapped_column(String(36), nullable=False)         # 이력서 ID
    answer: Mapped[str] = mapped_column(Text)                                  # 답변 내용
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


# ========================
# 회사 템플릿 응답 분석 (답변 요약, 키워드 등)
# ========================
class CompanyTemplateResponseAnalysis(Base):
    __tablename__ = "company_template_response_analysis"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    response_id: Mapped[str] = mapped_column(String(36), nullable=False)       # 응답 ID
    summary: Mapped[str] = mapped_column(Text)                                 # 요약
    keyword: Mapped[dict] = mapped_column(JSON)                                # 키워드 결과
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


# ========================
# 회사 요금제 (기업이 선택한 요금제 사용 정보)
# ========================
class CompanyPlan(Base):
    __tablename__ = "company_plan"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    plan_id: Mapped[str] = mapped_column(String(36), nullable=False)           # 요금제 ID
    company_id: Mapped[str] = mapped_column(String(36), nullable=False)        # 회사 ID
    start_date: Mapped[date] = mapped_column(Date, nullable=False)             # 시작일
    end_date: Mapped[date] = mapped_column(Date)                               # 종료일
    is_active: Mapped[bool] = mapped_column(Boolean, default=True)             # 활성 여부
    next_billing_date: Mapped[date] = mapped_column(Date)                      # 다음 결제 예정일


# ========================
# 포트폴리오 결과 (크롤링 + 키워드 처리)
# ========================
class PortfolioResult(Base):
    __tablename__ = "portfolio_result"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    crawling_result_id: Mapped[str] = mapped_column(String(36), nullable=False) # 크롤링 결과 ID
    processed_contents: Mapped[dict] = mapped_column(JSON)                      # 처리된 결과
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    field: Mapped[str] = mapped_column(String(255), nullable=False)             # 상태 필드


# ========================
# 회사 템플릿 (채용 템플릿)
# ========================
class CompanyTemplate(Base):
    __tablename__ = "company_template"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    company_id: Mapped[str] = mapped_column(String(36), nullable=False)        # 회사 ID
    evaluation_profile_id: Mapped[str] = mapped_column(String(36), nullable=False) # 평가 프로필 ID
    name: Mapped[str] = mapped_column(String(50), nullable=False)              # 템플릿 이름
    department: Mapped[str] = mapped_column(String(100), nullable=False)       # 부서
    category: Mapped[str] = mapped_column(String(100), nullable=False)         # 직무
    description: Mapped[str] = mapped_column(Text)                             # 직무 소개
    years_of_experience: Mapped[int] = mapped_column(Integer, default=0)       # 요구 연차
    status: Mapped[str] = mapped_column(String(20), default="DRAFT")           # 상태
    start_date: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow) # 시작일
    end_date: Mapped[datetime] = mapped_column(DateTime)                       # 종료일
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


# ========================
# 정합성 분석 결과
# ========================
class ValidationResult(Base):
    __tablename__ = "validation_result"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    validation_issue_id: Mapped[str] = mapped_column(String(36), nullable=False) # 이슈 ID
    resume_id: Mapped[str] = mapped_column(String(36), nullable=False)           # 이력서 ID
    validation_score: Mapped[float] = mapped_column(Float, nullable=False)       # 점수
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)


# ========================
# 사용자 초대 (기업 계정 초대 관리)
# ========================
class UserInvite(Base):
    __tablename__ = "user_invite"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    company_id: Mapped[str] = mapped_column(String(36), nullable=False)        # 회사 ID
    email: Mapped[str] = mapped_column(String(100), nullable=False)            # 초대 이메일
    role: Mapped[str] = mapped_column(String(50), nullable=False)              # 권한
    invite_token: Mapped[str] = mapped_column(String(512), nullable=False)     # 초대 토큰
    status: Mapped[str] = mapped_column(String(20), default="PENDING")         # 상태
    expires_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow) # 만료일
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)


# ========================
# 회사 폼 제출 (이력서 제출 내역)
# ========================
class CompanyFormSubmission(Base):
    __tablename__ = "company_form_submission"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    company_id: Mapped[str] = mapped_column(String(36), nullable=False)        # 회사 ID
    resume_id: Mapped[str] = mapped_column(String(36), nullable=False)         # 이력서 ID
    submitted_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow) # 제출 시각


# ========================
# 자격증 검증 결과
# ========================
class CertificateVerification(Base):
    __tablename__ = "certificate_verification"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    certificate_id: Mapped[str] = mapped_column(String(36), nullable=False)    # 자격증 ID
    status: Mapped[str] = mapped_column(String(20), default="PENDING")         # 검증 상태
    verification_source: Mapped[str] = mapped_column(String(255))              # 검증 근거
    verified_at: Mapped[datetime] = mapped_column(DateTime)                    # 검증 완료일
    error_message: Mapped[str] = mapped_column(Text)                           # 에러 메시지
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
