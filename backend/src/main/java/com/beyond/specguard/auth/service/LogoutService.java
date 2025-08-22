package com.beyond.specguard.auth.service;

import com.beyond.specguard.auth.repository.RefreshRepository;
import com.beyond.specguard.common.jwt.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    @Transactional
    public void logout(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        //  헤더 체크
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new JwtException("Authorization 헤더가 유효하지 않습니다.");
        }

        //  토큰 추출
        String accessToken = authorization.substring(7);

        //  토큰 유효성 검증
        if (jwtUtil.isExpired(accessToken)) {
            throw new JwtException("이미 만료된 Access Token입니다.");
        }

        String username = jwtUtil.getUsername(accessToken);

        refreshRepository.deleteByUsername(username);

        // Access Token 블랙리스트 처리 (Redis 붙이면 여기서 구현)
        // e.g., redisTemplate.opsForValue().set("BL:" + accessToken, "logout", duration);
    }
}
