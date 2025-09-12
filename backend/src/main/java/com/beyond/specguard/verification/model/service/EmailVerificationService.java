package com.beyond.specguard.verification.model.service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static com.beyond.specguard.verification.util.EmailUtil.normalizeEmail;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final StringRedisTemplate redis;
    private final JavaMailSender mail;

    @Value("${verify.ttl-seconds:300}")     long ttlSeconds;
    @Value("${verify.max-attempts:5}")      int maxAttempts;
    @Value("${verify.block-seconds:3600}")  long blockSeconds;
    @Value("${app.verify.mail-enabled:true}") boolean mailEnabled;

    private String codeKey(String e)    { return "verif:email:code:" + e; }
    private String attemptKey(String e) { return "verif:email:attempt:" + e; }
    private String blockKey(String e)   { return "verif:email:block:" + e; }

    @Transactional
    public void requestCode(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        if (Boolean.TRUE.equals(redis.hasKey(blockKey(email)))) throw new IllegalStateException("BLOCKED");

        String code = RandomStringUtils.randomNumeric(6);
        redis.opsForValue().set(codeKey(email), code, Duration.ofSeconds(ttlSeconds));
        redis.opsForValue().setIfAbsent(attemptKey(email), "0", Duration.ofSeconds(ttlSeconds));
        redis.expire(attemptKey(email), Duration.ofSeconds(ttlSeconds));

        if (!mailEnabled) { log.info("mail.disabled email={}", email); return; }

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(email);
            msg.setSubject("[SpecGuard] 이메일 인증코드");
            msg.setText("[SpecGuard] 인증코드: " + code + "\n유효시간: " + (ttlSeconds/60) + "분 안에 인증을 완료해주세요.");
            mail.send(msg);
            log.info("mail.sent email={}", email);
        } catch (MailException e) {
            log.error("mail.failed email={}", email, e);
        }
    }

    @Transactional
    public String confirm(String rawEmail, String inputCode) {
        String email = normalizeEmail(rawEmail);
        if (Boolean.TRUE.equals(redis.hasKey(blockKey(email)))) return "BLOCKED";

        String saved = redis.opsForValue().get(codeKey(email));
        if (saved == null) return "EXPIRED";

        if (saved.equals(inputCode)) {
            redis.delete(codeKey(email));
            redis.delete(attemptKey(email));
            return "SUCCESS";
        }

        long attempts = redis.opsForValue().increment(attemptKey(email));
        redis.expire(attemptKey(email), Duration.ofSeconds(ttlSeconds));
        if (attempts >= maxAttempts) {
            redis.opsForValue().set(blockKey(email), "1", Duration.ofSeconds(blockSeconds));
            redis.delete(codeKey(email));
            return "TOO_MANY_ATTEMPTS";
        }
        return "FAIL";
    }

    @PostConstruct
    void probe() {
        var impl = (JavaMailSenderImpl) mail;
        log.info("SMTP host={} port={} user={}", impl.getHost(), impl.getPort(), impl.getUsername());
    }
}
