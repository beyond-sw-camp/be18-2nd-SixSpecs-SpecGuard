package com.beyond.specguard.resume.exception.errorcode;

import com.beyond.specguard.common.exception.errorcode.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ResumeErrorCode implements ErrorCode {
    INVALID_CERTIFICATE(
            HttpStatus.BAD_REQUEST,
            "INVALID_CERTIFICATE",
            "잘못된 자격증 ID 요청입니다."
    ),
    UNAUTHORIZED_ACCESS(
            HttpStatus.UNAUTHORIZED,
            "UNAUTHORIZED_ACCESS",
            "인증에 실패했습니다."
    ),
    ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "ACCESS_DENIED",
            "접근이 불가능합니다."
    ),
    VERIFICATION_ALREADY_IN_PROGRESS(
            HttpStatus.CONFLICT,
            "VERIFICATION_ALREADY_IN_PROGRESS",
            "이미 검증 진행 중인 자격증입니다."
    ),
    VERIFICATION_REQUEST_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "VERIFICATION_REQUEST_FAILED",
            "서버 내부 오류"
    ),
    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "UNAUTHORIZED",
            "JWT 토큰 없음/만료/위조"
    ),
    FIELD_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "FIELD_NOT_FOUND",
            "문항이 없습니다."
    ),
    RESUME_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "RESUME_NOT_FOUND",
            "이력서가 없습니다."
    ),
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "서버 내부 오류"
    ),
    DUPLICATE_ENTRY(
            HttpStatus.CONFLICT,
            "DUPLICATE_ENTRY",
            "중복 입력입니다."
    ),
    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "INVALID_REQUEST",
            "필수값이 누락되거나 형식이 불일치합니다."
    ),
    UNSUPPORTED_MEDIA_TYPE(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "UNSUPPORTED_MEDIA_TYPE",
            "Content-Type이 잘못되었습니다."
    ),
    FILE_UPLOAD_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "FILE_UPLOAD_ERROR",
            "파일 저장 실패"
    ),
    DUPLICATE_EMAIL(
            HttpStatus.CONFLICT,
            "DUPLICATE_EMAIL",
            "해당 이메일은 이미 사용중입니다."
    ),
    INVALID_PARAMETER(
            HttpStatus.BAD_REQUEST,
            "INVALID_PARAMETER",
            "잘못된 페이징/정렬 파라미터입니다."
    ),
    ALREADY_SUBMITTED(
            HttpStatus.CONFLICT,
            "ALREADY_SUBMITTED",
            "이미 제출된 이력서입니다."
    ),
    DELETE_NOT_ALLOWED_BEFORE_DEADLINE(
            HttpStatus.CONFLICT,
            "DELETE_NOT_ALLOWED_BEFORE_DEADLINE",
            "마감 전에는 삭제할 수 없습니다."
    ),
    TEMPLATE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "TEMPLATE_NOT_FOUND",
            "템플릿이 없습니다."
    ),
    INVALID_RESUME_CREDENTIAL(HttpStatus.UNAUTHORIZED, "INVALID_RESUME_CREDENTIAL", "이력서 인증에 실패했습니다."),
    REQUIRED_FIELD_MISSING(HttpStatus.BAD_REQUEST, "REQUIRED_FIELD_MISSING", "필수 필드가 누락되었습니다."),
    FIELD_CONSTRAINT_VIOLATION(HttpStatus.BAD_REQUEST, "FIELD_CONSTRAINT_VIOLATION", "조건에 부합하지 않는 응답입니다.");





    private final HttpStatus status;
    private final String code;
    private final String message;

    ResumeErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
