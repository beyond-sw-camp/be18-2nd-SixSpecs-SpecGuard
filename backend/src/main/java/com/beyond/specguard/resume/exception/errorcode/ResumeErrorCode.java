package com.beyond.specguard.resume.exception.errorcode;

import com.beyond.specguard.common.exception.errorcode.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ResumeErrorCode implements ErrorCode {
    RESUME_NOT_FOUND(HttpStatus.NOT_FOUND, "RESUME_NOT_FOUND", "이력서를 찾을 수 없습니다."),
    INVALID_RESUME_CREDENTIAL(HttpStatus.UNAUTHORIZED, "INVALID_RESUME_CREDENTIAL", "이력서 인증에 실패했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ResumeErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
