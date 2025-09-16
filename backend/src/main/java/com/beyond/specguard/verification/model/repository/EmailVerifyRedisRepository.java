package com.beyond.specguard.verification.model.repository;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

public class EmailVerifyRedisRepository {
    private final StringRedisTemplate redis;
    private final long codeTtlSeconds;
    private final long attemptTtlSeconds;
    private final String codePrefix;
    private final String attemptPrefix;

    public EmailVerifyRedisRepository(StringRedisTemplate redis,
                                      long codeTtlSeconds, long attemptTtlSeconds,
                                      String codePrefix, String attemptPrefix) {
        this.redis = redis;
        this.codeTtlSeconds = codeTtlSeconds;
        this.attemptTtlSeconds = attemptTtlSeconds;
        this.codePrefix = codePrefix;
        this.attemptPrefix = attemptPrefix;
    }

    private String codeKey(String email){ return codePrefix + email.toLowerCase(); }
    private String attemptKey(String email){ return attemptPrefix + email.toLowerCase(); }

    public void saveCode(String email, String code){
        redis.opsForValue().set(codeKey(email), code, Duration.ofSeconds(codeTtlSeconds));
    }
    public String getCode(String email){ return redis.opsForValue().get(codeKey(email)); }
    public void deleteCode(String email){ redis.delete(codeKey(email)); }
    public long incrAttempt(String email){
        var k = attemptKey(email);
        Long n = redis.opsForValue().increment(k);
        if (n != null && n == 1L) redis.expire(k, Duration.ofSeconds(attemptTtlSeconds));
        return n == null ? 0L : n;
    }
    public long codeTtlSeconds(){ return codeTtlSeconds; }
}
