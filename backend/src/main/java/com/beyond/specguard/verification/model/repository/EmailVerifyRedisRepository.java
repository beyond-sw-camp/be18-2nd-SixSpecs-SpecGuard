package com.beyond.specguard.verification.model.repository;

import com.beyond.specguard.common.config.VerifyConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class EmailVerifyRedisRepository {
    private final StringRedisTemplate redis;
    private final VerifyConfig verifyConfig;
//    private final long codeTtlSeconds;
//    private final long attemptTtlSeconds;
//    private final String codePrefix;
//    private final String attemptPrefix;

    private String codeKey(String email){
        return verifyConfig.getCodePrefix()+ email.toLowerCase();
    }
    private String attemptKey(String email){
        return verifyConfig.getAttemptPrefix() + email.toLowerCase();
    }

    public void saveCode(String email, String code){
        redis.opsForValue().set(
                codeKey(email), code,
                Duration.ofSeconds(verifyConfig.getTtlSeconds())
        );
    }
    public String getCode(String email){
        return redis.opsForValue().get(codeKey(email));
    }
    public void deleteCode(String email){
        redis.delete(codeKey(email));
    }
    public long incrAttempt(String email){
        var k = attemptKey(email);
        Long n = redis.opsForValue().increment(k);
        if (n != null && n == 1L) redis.expire(k, Duration.ofSeconds(verifyConfig.getAttemptTtlSeconds()));
        return n == null ? 0L : n;
    }
}
