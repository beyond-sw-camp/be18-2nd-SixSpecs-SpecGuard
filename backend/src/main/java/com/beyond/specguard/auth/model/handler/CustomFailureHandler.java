package com.beyond.specguard.auth.model.handler;

import com.beyond.specguard.auth.exception.AuthException;
import com.beyond.specguard.auth.exception.errorcode.AuthErrorCode;
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
        throw new AuthException(AuthErrorCode.INVALID_LOGIN);
    }
}
