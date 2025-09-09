package com.beyond.specguard.resume.exception.errorcode;

import com.beyond.specguard.common.exception.errorcode.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ResumeErrorCode implements ErrorCode {
    RESUME_NOT_FOUND(HttpStatus.NOT_FOUND, "RESUME_NOT_FOUND", "이력서를 찾을 수 없습니다."),
    INVALID_RESUME_CREDENTIAL(HttpStatus.UNAUTHORIZED, "INVALID_RESUME_CREDENTIAL", "이력서 인증에 실패했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청입니다."),
    CORE_FIELDS_IMMUTABLE(org.springframework.http.HttpStatus.BAD_REQUEST,
            "CORE_FIELDS_IMMUTABLE", "이력서 기본 정보는 생성 후 수정할 수 없습니다."),
    DUPLICATED_SUBMISSION(org.springframework.http.HttpStatus.CONFLICT,
            "DUPLICATED_SUBMISSION", "이미 해당 회사에 제출된 이력서입니다."),
    MISSING_REQUIRED_SECTIONS(org.springframework.http.HttpStatus.BAD_REQUEST,
            "MISSING_REQUIRED_SECTIONS", "제출 요건이 충족되지 않았습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;

    ResumeErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
