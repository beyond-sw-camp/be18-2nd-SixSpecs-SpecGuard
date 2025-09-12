package com.beyond.specguard.verification.model.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static com.beyond.specguard.verification.util.EmailUtil.normalizeEmail;


@Service
@RequiredArgsConstructor
public class EmailVerificationService {

//    private final VerificationRedisRepository verificationRedisRepository;
    private final StringRedisTemplate redis;
    private final JavaMailSender mail;

    @Value("${verify.ttl-seconds:300}")     long ttlSeconds;
    @Value("${verify.max-attempts:5}")      int maxAttempts;
    @Value("${verify.block-seconds:3600}")  long blockSeconds;

    private String codeKey(String e)    { return "verif:email:code:" + e; }
    private String attemptKey(String e) { return "verif:email:attempt:" + e; }
    private String blockKey(String e)   { return "verif:email:block:" + e; }

    @Transactional
    public void requestCode(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        if (Boolean.TRUE.equals(redis.hasKey(blockKey(email))))
            throw new IllegalStateException("BLOCKED");

        String code = RandomStringUtils.randomNumeric(6);
        redis.opsForValue().set(codeKey(email), code, Duration.ofSeconds(ttlSeconds));
        // 시도횟수 키도 TTL 갱신(없으면 0으로 시작)
        // 시도카운터 초기화 및 TTL 정렬
        redis.opsForValue().setIfAbsent(attemptKey(email), "0", Duration.ofSeconds(ttlSeconds));
        redis.expire(attemptKey(email), Duration.ofSeconds(ttlSeconds));

        sendEmail(email, code);
    }

    @Transactional
    public String confirm(String rawEmail, String inputCode) {
        String email = normalizeEmail(rawEmail);
        if (Boolean.TRUE.equals(redis.hasKey(blockKey(email)))) return "BLOCKED";

        String k = codeKey(email);
        String saved = redis.opsForValue().get(k);
        if (saved == null) return "EXPIRED";

        if (saved.equals(inputCode)) {
            redis.delete(k);
            redis.delete(attemptKey(email));
            return "SUCCESS";
        }
        long attempts = redis.opsForValue().increment(attemptKey(email));
        redis.expire(attemptKey(email), Duration.ofSeconds(ttlSeconds));
        if (attempts >= maxAttempts) {
            redis.opsForValue().set(blockKey(email), "1", Duration.ofSeconds(blockSeconds));
            redis.delete(k);
            return "TOO_MANY_ATTEMPTS";
        }
        return "FAIL";
    }

//    @Transactional
//    public VerifyResult confirmCode(String rawEmail, String inputCode) {
//        String email = normalizeEmail(rawEmail);
//        if (Boolean.TRUE.equals(redis.hasKey(blockKey(email))))
//            return new VerifyResult("BLOCKED", "blocked");
//
//        String key = codeKey(email);
//        String saved = redis.opsForValue().get(key);
//        if (saved == null) return new VerifyResult("EXPIRED", "code expired");
//
//        if (saved.equals(inputCode)) {
//            redis.delete(key);
//            redis.delete(attemptKey(email));
//            return VerifyResult.ok();
//        }
//        // 실패 처리
//        long attempts = redis.opsForValue().increment(attemptKey(email));
//        // attempt 키 TTL 유지
//        redis.expire(attemptKey(email), Duration.ofSeconds(ttlSeconds));
//        if (attempts >= maxAttempts) {
//            redis.opsForValue().set(blockKey(email), "1", Duration.ofSeconds(blockSeconds));
//            redis.delete(key);
//            return new VerifyResult("TOO_MANY_ATTEMPTS", "blocked");
//        }
//        return new VerifyResult("FAIL", "invalid code");
//    }

    private void sendEmail(String to, String code) {
        var msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("[SpecGuard] 이메일 인증코드");
        msg.setText("인증코드: " + code + "\n유효시간: " + ttlSeconds + "초");
        mail.send(msg);
    }
}
