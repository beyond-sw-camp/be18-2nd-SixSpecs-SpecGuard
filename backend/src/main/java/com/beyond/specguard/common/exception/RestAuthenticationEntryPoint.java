package com.beyond.specguard.common.exception;

import com.beyond.specguard.common.exception.errorcode.AuthErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper om = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        System.out.println(">>> EntryPoint ex=" + ex.getClass() + ", msg=" + ex.getMessage());

        // 기본값
        AuthErrorCode errorCode = AuthErrorCode.UNAUTHORIZED;

        // AuthException이면 세부 errorCode 사용
        if (ex instanceof AuthException authEx) {
            errorCode = authEx.getErrorCode();
        }

        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse body = ErrorResponse.of(errorCode);
        om.writeValue(response.getWriter(), body);
    }
}
