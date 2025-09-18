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
    @Value("${verify.ttl-seconds}")
    private long ttlSeconds;

    @Value("${verify.redis.code-prefix}")
    private String codePrefix;

    @Value("${verify.redis.attempt-prefix}")
    private String attemptPrefix;

    @Value("${verify.redis.attempt-ttl-seconds}")
    private long attemptTtlSeconds;
}