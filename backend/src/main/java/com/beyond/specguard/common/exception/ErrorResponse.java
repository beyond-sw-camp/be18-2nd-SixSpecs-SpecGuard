package com.beyond.specguard.common.exception;

import com.beyond.specguard.common.exception.errorcode.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private final int status;     // HttpStatus 코드값
    private final String code;    // 에러 코드
    private final String message; // 에러 메시지

    // ✅ 기본 에러코드 그대로 사용
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage()
        );
    }

    // ✅ 기본 코드 + 커스텀 메시지 오버라이드
    public static ErrorResponse of(ErrorCode errorCode, String customMessage) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                customMessage
        );
    }
}
