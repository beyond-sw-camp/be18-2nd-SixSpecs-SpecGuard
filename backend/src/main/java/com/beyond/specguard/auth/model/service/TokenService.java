package com.beyond.specguard.auth.model.service;

import com.beyond.specguard.auth.exception.errorcode.AuthErrorCode;
import com.beyond.specguard.auth.model.dto.response.TokenResponseDto;
import com.beyond.specguard.auth.model.entity.ClientUser;
import com.beyond.specguard.auth.model.repository.ClientUserRepository;
import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.common.jwt.JwtUtil;
import com.beyond.specguard.common.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtUtil jwtUtil;
    private final ClientUserRepository clientUserRepository;
    private final RedisTokenService redisTokenService; // ✅ Redis 세션 관리

    public TokenResponseDto issueAccessToken(HttpServletRequest request) {
        // 1. 쿠키에서 refresh 추출
        String refreshToken = CookieUtil.getCookieValue(request, "refresh_token");
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 2. refresh 유효성 검증
        if (jwtUtil.isExpired(refreshToken)) {
            throw new CustomException(AuthErrorCode.REFRESH_TOKEN_EXPIRED);
        }
        if (!"refresh".equals(jwtUtil.getCategory(refreshToken))) {
            throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 3. 유저 조회 (username = email 역할)
        String username = jwtUtil.getUsername(refreshToken);
        ClientUser user = clientUserRepository.findByEmail(username)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // 4. 새 AccessToken 발급
        String newAccessToken = jwtUtil.createAccessToken(
                user.getEmail(),
                user.getRole().name(),
                user.getCompany().getSlug()
        );

        // 5. 단일 세션 관리 (Redis에 저장)
        String jti = jwtUtil.getJti(newAccessToken); // 새 AccessToken의 jti
        long accessTtl = jwtUtil.getExpiration(newAccessToken).getTime() - System.currentTimeMillis();

        // TTL은 millisecond → second 변환
        redisTokenService.saveUserSession(user.getEmail(), jti, accessTtl / 1000);

        return new TokenResponseDto(newAccessToken, "AccessToken 발급 성공");
    }
}
