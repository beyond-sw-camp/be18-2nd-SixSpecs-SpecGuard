package com.beyond.specguard.resume.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
//========임시=====
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,Object>> handleBadRequest(IllegalArgumentException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "status", HttpStatus.BAD_REQUEST.value(),
                        "error", "Bad Request",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String,Object>> handleConflict(IllegalStateException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "status", HttpStatus.CONFLICT.value(),
                        "error", "Conflict",
                        "message", ex.getMessage()
                ));
    }
}
