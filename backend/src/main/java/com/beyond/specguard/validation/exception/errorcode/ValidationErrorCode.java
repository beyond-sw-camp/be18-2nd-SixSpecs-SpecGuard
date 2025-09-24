package com.beyond.specguard.validation.exception.errorcode;

import com.beyond.specguard.common.exception.errorcode.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ValidationErrorCode implements ErrorCode {



    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "요청 본문 오류 (필드 누락/형식)"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증 실패 (JWT 만료/위조)"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "권한 없음 (OWNER/MANAGER 아님)"),
    RESUME_NOT_FOUND(HttpStatus.NOT_FOUND, "RESUME_NOT_FOUND", "이력서 없음 (미존재/접근 불가)"),
    NLP_PRECHECK_FAILED(HttpStatus.UNPROCESSABLE_ENTITY, "NLP_PRECHECK_FAILED", "사전 검증 실패 (필수 데이터 누락 등)"),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED", "요청 제한 초과 (조직/사용자 초과 요청)"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "내부 오류 (알 수 없는 오류)"),

    INVALID_INPUT_COMMENT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "본문 오류 (코멘트 길이 초과 등)"),
    RESUME_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "RESUME_NOT_FOUND", "결과 또는 연결 이력서 없음 (resultId 미존재)"),

    RESUME_MISMATCH(HttpStatus.NOT_FOUND, "RESUME_NOT_FOUND", "대상/분포 없음 (결과/이력서/템플릿 불일치)"),
    INVALID_REQUEST(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_REQUEST", "잘못된 상태 (adjusted_total 없음)");




    private final HttpStatus status;
    private final String code;
    private final String message;

    ValidationErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
