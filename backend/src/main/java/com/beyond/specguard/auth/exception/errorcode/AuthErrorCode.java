<<<<<<< HEAD
package com.beyond.specguard.auth.exception.errorcode;

import com.beyond.specguard.common.exception.errorcode.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements ErrorCode {
    //  회원가입 관련
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", "이미 등록된 이메일입니다."),
    DUPLICATE_COMPANY(HttpStatus.CONFLICT, "DUPLICATE_COMPANY", "이미 등록된 사업자 번호입니다."),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_EMAIL_FORMAT", "이메일 형식이 올바르지 않습니다."),
    WEAK_PASSWORD(HttpStatus.BAD_REQUEST, "WEAK_PASSWORD", "비밀번호 정책을 만족하지 않습니다."),
    INVALID_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "INVALID_NAME_REQUIRED", "이름은 필수 입력 값입니다."),
    INVALID_BUSINESS_NUMBER(HttpStatus.BAD_REQUEST, "INVALID_BUSINESS_NUMBER", "사업자번호 형식이 올바르지 않습니다."),

    // 로그인 관련
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "INVALID_LOGIN", "이메일 또는 비밀번호가 올바르지 않습니다."),

    // Refresh Token 관련
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "INVALID_REFRESH_TOKEN", "리프레시 토큰이 유효하지 않습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "REFRESH_TOKEN_EXPIRED", "리프레시 토큰이 만료되었습니다."),

    // Access Token 관련 (추가)
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_ACCESS_TOKEN", "액세스 토큰이 유효하지 않습니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "ACCESS_TOKEN_EXPIRED", "액세스 토큰이 만료되었습니다."),
    BLACKLISTED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "BLACKLISTED_ACCESS_TOKEN", "로그아웃된 액세스 토큰입니다."),
    INVALID_TOKEN_CATEGORY(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN_CATEGORY", "잘못된 토큰 유형입니다."),

    // 인증/인가 전역 실패
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "접근 권한이 없습니다."),

    // 기타
    DUPLICATE_SLUG(HttpStatus.CONFLICT, "DUPLICATE_SLUG", "이미 사용 중인 슬러그입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    DUPLICATE_COMPANY_NAME(HttpStatus.CONFLICT,"DUPLICATE_COMPANY_NAME" ,"이미 사용중인 회사명입니다" ), 
    DUPLICATE_EMAIL_IN_COMPANY(HttpStatus.CONFLICT,"DUPLICATE_EMAIL_IN_COMPANY","이미 내부에서 사용중인 이메일 입니다" );

    private final HttpStatus status;
    private final String code;
    private final String message;

    AuthErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
=======
package com.beyond.specguard.auth.exception.errorcode;

import com.beyond.specguard.common.exception.errorcode.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements ErrorCode {
    //  회원가입 관련
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", "이미 등록된 이메일입니다."),
    DUPLICATE_COMPANY(HttpStatus.CONFLICT, "DUPLICATE_COMPANY", "이미 등록된 사업자 번호입니다."),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_EMAIL_FORMAT", "이메일 형식이 올바르지 않습니다."),
    WEAK_PASSWORD(HttpStatus.BAD_REQUEST, "WEAK_PASSWORD", "비밀번호 정책을 만족하지 않습니다."),
    INVALID_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "INVALID_NAME_REQUIRED", "이름은 필수 입력 값입니다."),
    INVALID_BUSINESS_NUMBER(HttpStatus.BAD_REQUEST, "INVALID_BUSINESS_NUMBER", "사업자번호 형식이 올바르지 않습니다."),

    // 로그인 관련
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "INVALID_LOGIN", "이메일 또는 비밀번호가 올바르지 않습니다."),

    // Refresh Token 관련
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "INVALID_REFRESH_TOKEN", "리프레시 토큰이 유효하지 않습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "REFRESH_TOKEN_EXPIRED", "리프레시 토큰이 만료되었습니다."),

    // Access Token 관련 (추가)
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_ACCESS_TOKEN", "액세스 토큰이 유효하지 않습니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "ACCESS_TOKEN_EXPIRED", "액세스 토큰이 만료되었습니다."),
    BLACKLISTED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "BLACKLISTED_ACCESS_TOKEN", "로그아웃된 액세스 토큰입니다."),
    INVALID_TOKEN_CATEGORY(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN_CATEGORY", "잘못된 토큰 유형입니다."),

    // 인증/인가 전역 실패
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "접근 권한이 없습니다."),

    // 기타
    DUPLICATE_SLUG(HttpStatus.CONFLICT, "DUPLICATE_SLUG", "이미 사용 중인 슬러그입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    DUPLICATE_COMPANY_NAME(HttpStatus.CONFLICT,"DUPLICATE_COMPANY_NAME" ,"이미 사용중인 회사명입니다" ), 
    DUPLICATE_EMAIL_IN_COMPANY(HttpStatus.CONFLICT,"DUPLICATE_EMAIL_IN_COMPANY","이미 내부에서 사용중인 이메일 입니다" ),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 내부 처리 중 오류가 발생했습니다."),
    SESSION_CONFLICT(HttpStatus.CONFLICT ,"SESSION_CONFLICT","다른기기에서 로그인하여 로그아웃 되었습니다" );

    private final HttpStatus status;
    private final String code;
    private final String message;

    AuthErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
>>>>>>> develop
