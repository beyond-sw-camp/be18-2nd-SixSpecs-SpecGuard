package com.beyond.specguard.auth.handler;

import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.common.exception.errorcode.AuthErrorCode;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        // ✅ 전역 예외 핸들러가 처리하도록 CustomException 발생
        throw new CustomException(AuthErrorCode.INVALID_LOGIN);
    }
}
