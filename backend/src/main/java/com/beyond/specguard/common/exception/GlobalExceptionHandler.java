package com.beyond.specguard.common.exception;

import com.beyond.specguard.common.exception.errorcode.CommonErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ✅ 커스텀 예외 처리 (서비스 레벨에서 던진 CustomException)
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        return ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(ErrorResponse.of(ex.getErrorCode()));
    }

    // ✅ DTO 검증 실패 (ex: @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        // 필드 단위 오류 메시지 추출
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage()) // 👉 "이메일 형식이 올바르지 않습니다."
                .findFirst()
                .orElse("잘못된 요청입니다.");

        // CommonErrorCode.INVALID_REQUEST 사용
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        CommonErrorCode.INVALID_REQUEST.getCode(),  // "INVALID_REQUEST"
                        errorMessage
                ));
    }

    // ✅ 예상 못한 모든 예외 (서버 내부 오류)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(CommonErrorCode.UNEXPECTED_ERROR));
    }
}
