package com.beyond.specguard.auth.model.handler;

import com.beyond.specguard.common.exception.errorcode.ErrorCode;
import com.beyond.specguard.invite.exception.InviteException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        log.error(" OAuth2 로그인 실패: {}", exception.getMessage(), exception);

        String code = "OAUTH2_LOGIN_FAILED";
        String message = "OAuth2 로그인 중 오류가 발생했습니다.";

        Throwable cause = exception.getCause() != null ? exception.getCause() : exception;
        if (cause instanceof InviteException inviteEx) {
            code = inviteEx.getErrorCode().getCode();
            message = inviteEx.getErrorCode().getMessage();
        }

        // ✅ state 값에서 inviteToken 추출
        String state = request.getParameter("state");
        String inviteToken = null;
        if (state != null && state.contains("__")) {
            inviteToken = state.split("__")[1];
        }

        // ✅ 프론트 실패 페이지로 Redirect (token 포함)
        String redirectUrl = String.format(
                "http://localhost:5173/oauth2/failure?code=%s&message=%s&token=%s",
                code,
                URLEncoder.encode(message, StandardCharsets.UTF_8),
                inviteToken != null ? inviteToken : ""
        );

        log.info("🔄 OAuth2 실패 → Redirect: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
