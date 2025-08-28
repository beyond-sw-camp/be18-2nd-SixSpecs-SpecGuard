package com.beyond.specguard.auth.service;

import com.beyond.specguard.common.jwt.JwtUtil;
import com.beyond.specguard.common.util.CookieUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final JwtUtil jwtUtil;
    private final RedisTokenService redisTokenService; // ✅ DB 대신 Redis 사용

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String authorization = request.getHeader("Authorization");

        //  헤더 체크
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new JwtException("Authorization 헤더가 유효하지 않습니다.");
        }

        //  Access Token 추출
        String accessToken = authorization.substring(7);

        //  토큰 만료 여부 확인
        if (jwtUtil.isExpired(accessToken)) {
            throw new JwtException("이미 만료된 Access Token입니다.");
        }

        //  사용자 식별자 추출
        String username = jwtUtil.getUsername(accessToken);

        // ✅ Refresh Token 삭제 (Redis)
        redisTokenService.deleteRefreshToken(username);

        // ✅ Refresh Token 쿠키 삭제
        response.addCookie(CookieUtil.deleteCookie("refresh_token"));

        // ✅ Access Token 블랙리스트 등록
        String jti = jwtUtil.getJti(accessToken); // Access Token의 jti 추출
        long ttl = (jwtUtil.getExpiration(accessToken).getTime() - System.currentTimeMillis()) / 1000; // 남은 만료 시간
        redisTokenService.blacklistAccessToken(jti, ttl);
    }
}
