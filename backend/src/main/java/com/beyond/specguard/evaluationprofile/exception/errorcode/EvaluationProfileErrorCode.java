package com.beyond.specguard.evaluationprofile.exception.errorcode;

import com.beyond.specguard.common.exception.errorcode.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum EvaluationProfileErrorCode implements ErrorCode{
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 에러"),
    EVALUATION_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "EVALUATION_PROFILE_NOT_FOUND", "해당 EvaluationProfile을 찾을 수 없습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "프로필 관련 접근할 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    EvaluationProfileErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
