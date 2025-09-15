package com.beyond.specguard.auth.model.handler.local;

import com.beyond.specguard.admin.model.service.InternalAdminDetails;
import com.beyond.specguard.company.common.model.service.CustomUserDetails;
import com.beyond.specguard.company.common.model.service.RedisTokenService;
import com.beyond.specguard.common.jwt.JwtUtil;
import com.beyond.specguard.common.util.CookieUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RedisTokenService redisTokenService; // Redis 사용

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String email;
        String role;
        String companySlug = null;

        log.debug(authentication.getName());

        // 🔹 인증 대상 분기 처리
        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            email = userDetails.getUsername();
            role = userDetails.getUser().getRole().name();
            companySlug = userDetails.getUser().getCompany().getSlug(); // 기업 유저만 존재

        } else if (authentication.getPrincipal() instanceof InternalAdminDetails adminDetails) {
            email = adminDetails.getAdmin().getEmail();
            role = adminDetails.getAdmin().getRole().name();
            // Admin은 회사 정보 없음
        } else {
            throw new IllegalStateException("Unknown principal type: " + authentication.getPrincipal().getClass());
        }

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
        Date accessExpiration = jwtUtil.getExpiration(accessToken);
        long accessTtl = (accessExpiration.getTime() - System.currentTimeMillis()) / 1000;
        redisTokenService.saveUserSession(email, accessJti, accessTtl);

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
