package com.beyond.specguard.verification.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

//@Order(Ordered.HIGHEST_PRECEDENCE)
@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.beyond.specguard.controller")
public class VerifyExceptionHandler {

    @ExceptionHandler(VerifyNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleNotFound(VerifyNotFoundException e){
        return ResponseEntity.status(404).body(Map.of("status","FAIL","reason", e.getMessage()));
    }
    @ExceptionHandler(VerifyExpiredException.class)
    public ResponseEntity<Map<String,String>> handleExpired(VerifyExpiredException e){
        return ResponseEntity.status(410).body(Map.of("status","FAIL","reason", e.getMessage()));
    }
    @ExceptionHandler({VerifyInvalidTokenException.class, VerifyInvalidPhoneException.class})
    public ResponseEntity<Map<String,String>> handleBadReq(VerifyException e){
        return ResponseEntity.status(400).body(Map.of("status","FAIL","reason", e.getMessage()));
    }
    @ExceptionHandler(VerifyDeliveryPendingException.class)
    public ResponseEntity<Map<String,String>> handlePending(VerifyDeliveryPendingException e){
        return ResponseEntity.status(409).body(Map.of("status","FAIL","reason", e.getMessage()));
    }

    // ★ 우리가 정의하지 않은 런타임 예외가 터질 때 500을 깔끔히 반환 (원인 노출 방지)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,String>> handleUnknown(Exception e){
        // 👇 반드시 로그 찍기
        org.slf4j.LoggerFactory.getLogger(VerifyExceptionHandler.class)
                .error("[VERIFY] Unhandled exception", e);

        // 디버그용으로 예외 타입만 잠깐 내려보면 원인 파악이 빨라요 (운영에선 제거!)
        return ResponseEntity.status(500).body(Map.of(
                "status","FAIL",
                "reason","INTERNAL_ERROR",
                "type", e.getClass().getSimpleName()  // 임시
        ));
    }

}