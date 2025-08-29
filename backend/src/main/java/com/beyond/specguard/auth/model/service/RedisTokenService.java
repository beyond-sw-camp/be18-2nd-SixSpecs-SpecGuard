package com.beyond.specguard.auth.model.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    // Key Prefix (프로젝트명 기준으로 명확하게)
    private static final String REFRESH_PREFIX = "specguard:refresh:";
    private static final String BLACKLIST_PREFIX = "specguard:blacklist:";

    // ================== Refresh Token 관리 ==================

    // 저장 (username 기준)
    public void saveRefreshToken(String username, String refreshToken, long ttlSeconds) {
        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + username,
                refreshToken,
                ttlSeconds,
                TimeUnit.SECONDS
        );
    }

    // 조회
    public String getRefreshToken(String username) {
        return redisTemplate.opsForValue().get(REFRESH_PREFIX + username);
    }

    // 삭제
    public void deleteRefreshToken(String username) {
        redisTemplate.delete(REFRESH_PREFIX + username);
    }

    // ================== Access Token 블랙리스트 관리 ==================

    // 블랙리스트 등록 (jti 기준)
    public void blacklistAccessToken(String jti, long ttlSeconds) {
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + jti,
                "logout",
                ttlSeconds,
                TimeUnit.SECONDS
        );
    }

    // 블랙리스트 조회
    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + jti));
    }
}
