package com.beyond.specguard.auth.model.handler;

import com.beyond.specguard.auth.model.service.CustomUserDetails;
import com.beyond.specguard.auth.model.service.RedisTokenService;
import com.beyond.specguard.common.jwt.JwtUtil;
import com.beyond.specguard.common.util.CookieUtil;
import jakarta.servlet.ServletException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RedisTokenService redisTokenService; //  Redis 사용

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        String role = userDetails.getUser().getRole().name();
        String companySlug = userDetails.getUser().getCompany().getSlug();

        //  토큰 생성
        String access = jwtUtil.createAccessToken(email, role, companySlug);
        String refresh = jwtUtil.createRefreshToken(email);

        //  Refresh 저장 (Redis)
        Date refreshExpiration = jwtUtil.getExpiration(refresh);
        long ttl = (refreshExpiration.getTime() - System.currentTimeMillis()) / 1000;
        redisTokenService.saveRefreshToken(email, refresh, ttl);

        //  Access Token → Authorization 헤더
        response.setHeader("Authorization", "Bearer " + access);

        //  Refresh Token → HttpOnly, Secure, SameSite=None 쿠키
        int maxAge = (int) ttl;
        response.addCookie(
                CookieUtil.createHttpOnlyCookie("refresh_token", refresh, maxAge)
        );

        //  상태 코드만 반환
        response.setStatus(HttpStatus.OK.value());
    }
}
