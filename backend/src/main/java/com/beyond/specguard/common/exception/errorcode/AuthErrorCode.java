package com.beyond.specguard.common.exception.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements ErrorCode {
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "DUPLICATE_EMAIL", "이미 등록된 이메일"),
    INVALID_EMAIL_FORMAT(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_EMAIL_FORMAT", "이메일 형식이 잘못됨"),
    WEAK_PASSWORD(HttpStatus.UNPROCESSABLE_ENTITY, "WEAK_PASSWORD", "비밀번호 보안 기준 미달");

    private final HttpStatus status;
    private final String code;
    private final String message;

    AuthErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
