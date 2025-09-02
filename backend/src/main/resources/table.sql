DROP TABLE IF EXISTS `company_template`;
DROP TABLE IF EXISTS `company_template_field`;

CREATE TABLE `company_template` (
        `id`	CHAR(36) PRIMARY KEY,
        `company_id`	CHAR(36)	NOT NULL	COMMENT '고객 회사 pk',
        `name`	VARCHAR(50)	NOT NULL	COMMENT '템플릿 이름',
        `department`	VARCHAR(100)	NOT NULL	COMMENT '부서',
        `category`	VARCHAR(100)	NOT NULL	COMMENT '직무',
        `description`	TEXT	NULL	COMMENT '직무 소개',
        `years_of_experience`	INT	NOT NULL	DEFAULT 0	COMMENT '요구 연차',
        `start_date`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '시작 시기',
        `end_date`	DATETIME	NOT NULL	COMMENT '마감 시기',
        `is_active`	BOOLEAN	NOT NULL	DEFAULT FALSE	COMMENT '사용 가능 여부',
        `created_at`	TIMESTAMP	NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '생성 시기',
        `updated_at`	TIMESTAMP	NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '업데이트 시기'
);


CREATE TABLE `company_template_field` (
          `id`	CHAR(36) PRIMARY KEY,
          `template_id`	CHAR(36)	NOT NULL	COMMENT '템플릿 PK',
          `field_name`	VARCHAR(100)	NOT NULL	COMMENT '필드 이름',
          `field_type`	ENUM('TEXT', 'NUMBER', 'DATE', 'SELECT' ) NOT NULL DEFAULT 'TEXT' COMMENT '필드 타입',
          `is_required`	BOOLEAN	NULL	DEFAULT FALSE	COMMENT '필수 여부',
          `field_order`	INT	NULL	COMMENT '출력 순서',
          `options`	JSON	NULL	COMMENT '선택지(라디오, 드롭다운 등)',
          `min_length`	INT	NULL	DEFAULT 0	COMMENT '최소 길이',
          `max_length`	INT	NULL	DEFAULT 500	COMMENT '최대 길이',
          `created_at`	TIMESTAMP	NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '생성 시기'
);


-- ALTER TABLE `company_template` ADD CONSTRAINT `FK_client_company_TO_company_template_1` FOREIGN KEY (
--      `company_id`
-- )
-- REFERENCES `client_company` (
--      `id`
-- );
--
ALTER TABLE `company_template_field` ADD CONSTRAINT `FK_company_template_TO_company_template_field_1` FOREIGN KEY (
   `template_id`
)
REFERENCES `company_template` (
   `id`
);

