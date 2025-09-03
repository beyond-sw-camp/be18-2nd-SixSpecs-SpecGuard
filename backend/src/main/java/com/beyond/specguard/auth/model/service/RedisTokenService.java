package com.beyond.specguard.auth.model.service;

import com.beyond.specguard.common.properties.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private final AppProperties appProperties;

    // ================== Refresh Token 관리 ==================

    // 저장 (username 기준)
    public void saveRefreshToken(String username, String refreshToken, long ttlSeconds) {
        redisTemplate.opsForValue().set(
                appProperties.getRedis().getPrefix().getRefresh() + username,
                refreshToken,
                ttlSeconds,
                TimeUnit.SECONDS
        );
    }

    // 조회
    public String getRefreshToken(String username) {
        return redisTemplate.opsForValue().get(
                appProperties.getRedis().getPrefix().getRefresh() + username
        );
    }

    // 삭제
    public void deleteRefreshToken(String username) {
        redisTemplate.delete(
                appProperties.getRedis().getPrefix().getRefresh() + username
        );
    }

    // ================== Access Token 블랙리스트 관리 ==================

    // 블랙리스트 등록 (jti 기준)
    public void blacklistAccessToken(String jti, long ttlSeconds) {
        redisTemplate.opsForValue().set(
                appProperties.getRedis().getPrefix().getBlacklist() + jti,
                "logout",
                ttlSeconds,
                TimeUnit.SECONDS
        );
    }

    // 블랙리스트 조회
    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(appProperties.getRedis().getPrefix().getBlacklist() + jti)
        );
    }
}
