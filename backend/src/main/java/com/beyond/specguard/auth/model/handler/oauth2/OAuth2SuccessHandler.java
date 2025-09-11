package com.beyond.specguard.auth.model.handler.oauth2;

import com.beyond.specguard.auth.model.service.oauth2.CustomOAuth2UserDetails;
import com.beyond.specguard.auth.model.service.common.RedisTokenService;
import com.beyond.specguard.common.util.JwtUtil;
import com.beyond.specguard.common.properties.AppProperties;
import com.beyond.specguard.common.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final AppProperties appProperties;
    private final RedisTokenService redisTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomOAuth2UserDetails customUser = (CustomOAuth2UserDetails) authentication.getPrincipal();
        String email = customUser.getEmail();

        //  RefreshToken 발급
        String refreshToken = jwtUtil.createRefreshToken(email);
        long refreshTtl = (jwtUtil.getExpiration(refreshToken).getTime() - System.currentTimeMillis()) / 1000;

        //  Redis에 저장 (username → RefreshToken)
        redisTokenService.saveRefreshToken(email, refreshToken, refreshTtl);

        //  HttpOnly Cookie 저장
        Cookie refreshCookie = CookieUtil.createHttpOnlyCookie("refresh_token", refreshToken, (int) refreshTtl);
        response.addCookie(refreshCookie);

        log.info(" OAuth2 로그인 성공: email={}, RefreshToken 발급 및 Redis 저장 완료", email);

        //  프론트엔드로 redirect (AccessToken은 별도 API에서 발급)
        response.sendRedirect("http://localhost:5173/oauth2/redirect");
    }
}