DROP TABLE IF EXISTS `resume_certificate`;
DROP TABLE IF EXISTS `certificate_verfication`;

CREATE TABLE `resume_certificate` (
      `id`	CHAR(36)	PRIMARY KEY DEFAULT UUID(),
      `resume_id`	CHAR(36)	NOT NULL	COMMENT '이력서 PK',
      `certificate_name`	VARCHAR(255)	NOT NULL	COMMENT '자격증 명',
      `certificate_number`	VARCHAR(255)	NOT NULL	COMMENT '자격증 발급 번호',
      `issuer`	VARCHAR(255)	NOT NULL	COMMENT '발행자',
      `issued_date`	DATE	NOT NULL	COMMENT '취득 시기',
      `cert_url`	TEXT	NULL	COMMENT '자격증 URL',
      `created_at`	TIMESTAMP	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '생성 시기',
      `updated_at`	TIMESTAMP	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '업데이트 시기'
);

CREATE TABLE `certificate_verification` (
    `id`	CHAR(36)	PRIMARY KEY 	DEFAULT UUID(),
    `certificate_id`	CHAR(36)	NOT NULL	COMMENT '자격증 PK',
    `status`	ENUM('PENDING', 'SUCCESS', 'FAILED') NOT NULL DEFAULT 'PENDING',
    `verification_source`	VARCHAR(255)	NULL	COMMENT '검증 근거 (예: "KISA API", "HRD-Net")',
    `verified_at`	DATETIME	NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '검증 완료 시각',
    `error_message`	TEXT	NULL	COMMENT '실패 시 에러 사유',
    `created_at`	TIMESTAMP	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '생성 시기',
    `updated_at`	TIMESTAMP	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '업데이트 시기'
);