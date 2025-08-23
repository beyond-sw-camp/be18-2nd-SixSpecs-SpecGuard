package com.beyond.specguard.common.exception.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements ErrorCode {
    // ✅ 회원가입 관련
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", "이미 등록된 이메일입니다."),
    DUPLICATE_COMPANY(HttpStatus.CONFLICT, "DUPLICATE_COMPANY", "이미 등록된 사업자 번호입니다."),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_EMAIL_FORMAT", "이메일 형식이 올바르지 않습니다."),
    WEAK_PASSWORD(HttpStatus.BAD_REQUEST, "WEAK_PASSWORD", "비밀번호 정책을 만족하지 않습니다."),
    INVALID_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "INVALID_NAME_REQUIRED", "이름은 필수 입력 값입니다."),
    INVALID_BUSINESS_NUMBER(HttpStatus.BAD_REQUEST, "INVALID_BUSINESS_NUMBER", "사업자번호 형식이 올바르지 않습니다."),

    // ✅ 로그인 관련
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "INVALID_LOGIN", "이메일 또는 비밀번호가 올바르지 않습니다."),

    // ✅ 토큰 관련
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "리프레시 토큰이 유효하지 않습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.FORBIDDEN, "REFRESH_TOKEN_EXPIRED", "리프레시 토큰이 만료되었습니다."),

    // ✅ 인증/인가 전역 실패
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "접근 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    AuthErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
