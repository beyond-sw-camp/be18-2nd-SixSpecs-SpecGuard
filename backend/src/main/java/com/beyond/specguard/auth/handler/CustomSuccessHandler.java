package com.beyond.specguard.auth.handler;

import com.beyond.specguard.auth.entity.RefreshEntity;
import com.beyond.specguard.auth.repository.RefreshRepository;
import com.beyond.specguard.auth.service.CustomUserDetails;
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
    private final RefreshRepository refreshRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        String role = userDetails.getUser().getRole().name();

        // ✅ 토큰 생성 (JwtUtil public 메서드 사용)
        String access = jwtUtil.createAccessToken(email, role);
        String refresh = jwtUtil.createRefreshToken(email, role);

        // ✅ Refresh 저장 (DB) → JwtUtil에서 추출한 만료일 그대로 반영
        Date refreshExpiration = jwtUtil.getExpiration(refresh);
        RefreshEntity refreshEntity = RefreshEntity.builder()
                .username(email)
                .refresh(refresh)
                .expiration(refreshExpiration)
                .build();
        refreshRepository.save(refreshEntity);

        // ✅ Access Token → Authorization 헤더 (Bearer 방식)
        response.setHeader("Authorization", "Bearer " + access);

        // ✅ Refresh Token → HttpOnly, Secure, SameSite=None 쿠키
        // maxAge는 "남은 시간"을 초 단위로 계산해서 설정
        int maxAge = (int) ((refreshExpiration.getTime() - System.currentTimeMillis()) / 1000);
        response.addCookie(
                CookieUtil.createHttpOnlyCookie("refresh_token", refresh, maxAge)
        );

        // ✅ 응답 상태만 내려주기
        response.setStatus(HttpStatus.OK.value());
    }
}
