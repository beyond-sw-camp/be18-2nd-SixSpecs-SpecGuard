package com.beyond.specguard.result.exception.errorcode;

import com.beyond.specguard.common.exception.errorcode.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ValidationErrorCode implements ErrorCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SG_INTERNAL_ERROR", "서버 내부 오류"),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "SG_INVALID_INPUT", "요청 본문 오류"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "SG_UNAUTHORIZED", "인증 실패"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "SG_FORBIDDEN", "권한 없음"),
    RESUME_NOT_FOUND(HttpStatus.NOT_FOUND, "SG_RESUME_NOT_FOUND", "이력서 없음"),
    DUPLICATE_OPERATION(HttpStatus.CONFLICT, "SG_DUPLICATE_OPERATION", "충돌"),
    NLP_PRECHECK_FAILED(HttpStatus.UNPROCESSABLE_ENTITY, "SG_NLP_PRECHECK_FAILED", "사전 검증 실패"),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "SG_RATE_LIMITED", "요청 제한 초과");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ValidationErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
