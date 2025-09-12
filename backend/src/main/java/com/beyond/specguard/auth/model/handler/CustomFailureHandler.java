package com.beyond.specguard.auth.model.handler;

import com.beyond.specguard.auth.exception.errorcode.AuthErrorCode;
import com.beyond.specguard.common.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class CustomFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper om = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        AuthErrorCode errorCode = AuthErrorCode.INVALID_LOGIN;

        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse body = ErrorResponse.of(errorCode);
        om.writeValue(response.getWriter(), body);

        System.out.println(">>> 로그인 실패: " + errorCode.getMessage());
    }
}

