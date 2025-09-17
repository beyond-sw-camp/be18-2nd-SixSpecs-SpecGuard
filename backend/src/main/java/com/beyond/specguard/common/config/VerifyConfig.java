package com.beyond.specguard.common.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.beyond.specguard.verification.model.repository.EmailVerifyRedisRepository;


@Configuration
@Getter
public class VerifyConfig {
    @Value("${verify.ttl-seconds:300}")
    private long ttlSeconds;

    @Value("${verify.redis.code-prefix:verif:email:}")
    private String codePrefix;

    @Value("${verify.redis.attempt-prefix:verif:attempt:}")
    private String attemptPrefix;

    @Value("${verify.redis.attempt-ttl-seconds:3600}")
    private long attemptTtlSeconds;
}