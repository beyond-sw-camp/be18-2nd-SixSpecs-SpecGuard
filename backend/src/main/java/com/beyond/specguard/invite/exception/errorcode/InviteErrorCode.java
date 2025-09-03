package com.beyond.specguard.invite.exception.errorcode;

import com.beyond.specguard.common.exception.errorcode.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InviteErrorCode implements ErrorCode {

    //  이메일 관련
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_EMAIL_FORMAT", "이메일 형식이 올바르지 않습니다."),

    //  권한 관련
    FORBIDDEN_INVITE(HttpStatus.FORBIDDEN, "FORBIDDEN_INVITE", "해당 역할은 초대를 보낼 수 없습니다."),

    //  회사 관련
    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "COMPANY_NOT_FOUND", "존재하지 않는 회사입니다."),

    //  초대 중복
    ALREADY_INVITED(HttpStatus.CONFLICT, "ALREADY_INVITED", "이미 초대된 이메일입니다."),

    //  초대 토큰 관련
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", "유효하지 않은 초대 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.FORBIDDEN, "EXPIRED_TOKEN", "초대 토큰이 만료되었습니다."),
    ALREADY_ACCEPTED(HttpStatus.CONFLICT, "ALREADY_ACCEPTED", "이미 처리된 초대입니다.");

    private final HttpStatus status;  //  이름 맞춤
    private final String code;
    private final String message;
}
