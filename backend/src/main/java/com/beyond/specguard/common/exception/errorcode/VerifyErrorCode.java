package com.beyond.specguard.common.exception.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum VerifyErrorCode implements ErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 에러"),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "잘못된 입력 값"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "접근 권한이 없습니다"),

    INVALID_PHONE(HttpStatus.BAD_REQUEST, "INVALID_PHONE_", "유효하지 않은 휴대폰 번호입니다."),
    INVALID_OTP_CODE(HttpStatus.BAD_REQUEST, "INVALID_OTP_CODE", "유효하지 않은 인증코드입니다."),
    OTP_EXPIRED(HttpStatus.BAD_REQUEST, "OTP_EXPIRE", "인증코드가 만료되었습니다."),
    VERIFY_NOT_FOUND(HttpStatus.NOT_FOUND, "VERIFY_NOT_FOUND", "인증 요청을 찾을 수 없습니다"),
    DELIVERY_PENDING(HttpStatus.NOT_FOUND, "DELIVERY_PENDING", "인증 코드를 찾을 수 없습니다."),
    VERIFY_ALREADY_USED(HttpStatus.BAD_REQUEST, "VERIFY_ALREADY_USED", "이미 사용된 인증입니다"),

    // 중복/기타
    DUPLICATE_REQUEST(HttpStatus.CONFLICT, "DUPLICATE_REQUEST", "이미 처리 중인 요청이 존재합니다"),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS", "요청 횟수를 초과했습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;

    VerifyErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
