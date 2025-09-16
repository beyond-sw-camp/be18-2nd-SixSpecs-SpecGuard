package com.beyond.specguard.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.beyond.specguard.verification.model.repository.EmailVerifyRedisRepository;


@Configuration
public class VerifyConfig {
    @Bean
    public EmailVerifyRedisRepository emailVerifyRedisRepository(
            StringRedisTemplate redis,
            @Value("${verify.ttl-seconds:300}") long ttlSeconds,
            @Value("${verify.redis.code-prefix:verif:email:}") String codePrefix,
            @Value("${verify.redis.attempt-prefix:verif:attempt:}") String attemptPrefix,
            @Value("${verify.redis.attempt-ttl-seconds:3600}") long attemptTtlSeconds) {

        return new EmailVerifyRedisRepository(redis, ttlSeconds, attemptTtlSeconds, codePrefix, attemptPrefix);
    }

    @Bean
    public long verifyCodeTtlSeconds(@Value("${verify.ttl-seconds:300}") long ttl) {
        return ttl;
    }
}