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

        AuthErrorCode errorCode;

        if (ex instanceof AuthException authEx) {
            errorCode = authEx.getErrorCode();
        } else if (ex instanceof org.springframework.security.authentication.BadCredentialsException) {
            errorCode = AuthErrorCode.INVALID_ACCESS_TOKEN;
        } else if (ex instanceof org.springframework.security.authentication.InsufficientAuthenticationException) {
            errorCode = AuthErrorCode.UNAUTHORIZED;
        } else {
            errorCode = AuthErrorCode.UNAUTHORIZED;
        }

        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse body = ErrorResponse.of(errorCode);
        om.writeValue(response.getWriter(), body);
    }
}
