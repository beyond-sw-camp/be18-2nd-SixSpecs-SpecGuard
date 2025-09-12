package com.beyond.specguard.verification.model.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.beyond.specguard.verification.model.service.VerifySendGridService;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final StringRedisTemplate redis;
    private final VerifySendGridService mailer;

    @Value("${verify.ttl-seconds:300}")
    private long ttlSeconds;

    private String key(String email) { return "verif:email:" + email.toLowerCase(); }
    private String attemptKey(String email) { return "verif:attempt:" + email.toLowerCase(); }

    public void requestCode(String email) throws IOException {
        String code = RandomStringUtils.randomNumeric(6);
        var k = key(email);
        redis.opsForValue().set(k, code, Duration.ofSeconds(ttlSeconds));
        log.info("verify.set key={} code={} ttl={}", k, code, ttlSeconds);
        mailer.sendCodeEmail(email, code, ttlSeconds);
    }

    public boolean verify(String email, String input) {
        String saved = redis.opsForValue().get(key(email));
        if (saved == null) return false;
        boolean ok = saved.equals(input);
        if (ok) redis.delete(key(email));
        else redis.opsForValue().increment(attemptKey(email)); // 선택: 실패 카운트
        return ok;
    }
}

