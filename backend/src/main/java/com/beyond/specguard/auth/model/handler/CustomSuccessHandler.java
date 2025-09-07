package com.beyond.specguard.auth.model.handler;

import com.beyond.specguard.auth.model.service.CustomUserDetails;
import com.beyond.specguard.auth.model.service.RedisTokenService;
import com.beyond.specguard.common.util.JwtUtil;
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
    private final RedisTokenService redisTokenService; // Redis 사용

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        String role = userDetails.getUser().getRole().name();
        String companySlug = userDetails.getUser().getCompany().getSlug();

        // 1. 토큰 생성
        String accessToken = jwtUtil.createAccessToken(email, role, companySlug);
        String refreshToken = jwtUtil.createRefreshToken(email);

        // 2. AccessToken jti 추출
        String accessJti = jwtUtil.getJti(accessToken);

        // 3. 기존 세션/토큰 제거
        redisTokenService.deleteUserSession(email);
        redisTokenService.deleteRefreshToken(email);

        // 4. 새로운 Refresh 저장
        Date refreshExpiration = jwtUtil.getExpiration(refreshToken);
        long refreshTtl = (refreshExpiration.getTime() - System.currentTimeMillis()) / 1000;
        redisTokenService.saveRefreshToken(email, refreshToken, refreshTtl);

        // 5. 세션 생성
        redisTokenService.saveUserSession(email, accessJti, refreshTtl);

        // 6. Access Token → Authorization 헤더
        response.setHeader("Authorization", "Bearer " + accessToken);

        // 7. Refresh Token → HttpOnly, Secure, SameSite=None 쿠키
        int maxAge = (int) refreshTtl;
        response.addCookie(
                CookieUtil.createHttpOnlyCookie("refresh_token", refreshToken, maxAge)
        );

        // 8. 상태 코드만 반환
        response.setStatus(HttpStatus.OK.value());
    }
}
