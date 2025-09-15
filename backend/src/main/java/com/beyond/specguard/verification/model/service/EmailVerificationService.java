package com.beyond.specguard.verification.model.service;

import com.beyond.specguard.verification.model.entity.ApplicantEmailVerification;
import com.beyond.specguard.verification.model.entity.CompanyEmailVerification;
import com.beyond.specguard.verification.model.entity.EmailVerifyStatus;
import com.beyond.specguard.verification.model.repository.ApplicantEmailVerificationRepo;
import com.beyond.specguard.verification.model.repository.CompanyEmailVerificationRepo;
import com.beyond.specguard.verification.model.type.VerifyTarget;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Getter
@Setter
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final StringRedisTemplate redis;
    private final VerifySendGridService mailer;

    private final ApplicantEmailVerificationRepo applicantRepo;
    private final CompanyEmailVerificationRepo companyRepo;

    @Value("${verify.ttl-seconds:300}")
    private long ttlSeconds;

    private static String norm(String e){ return e == null ? null : e.trim().toLowerCase(); }
    private String key(String email) { return "verif:email:" + email.toLowerCase(); }
    private String attemptKey(String email) { return "verif:attempt:" + email.toLowerCase(); }

    @Transactional
    public void requestCode(String rawEmail, VerifyTarget target, String ip) {
        final String email = norm(rawEmail);
        final String code  = RandomStringUtils.randomNumeric(6);
        final String k = key(email);
        redis.opsForValue().set(k, code, Duration.ofSeconds(ttlSeconds));
        log.info("verify.set key={} code(last2)=**{} ttl={}", k, code.substring(4), ttlSeconds);

        upsertPending(rawEmail, target, ip);
        mailer.sendCodeEmail(email, code, ttlSeconds); // unchecked 예외 전파


    }

    @Transactional
    public boolean verify(String rawEmail, String input, VerifyTarget target) {
        final String email = norm(rawEmail);
        final String k = key(email);
        String saved = redis.opsForValue().get(k);
        if (saved == null) return false;

        boolean ok = saved.equals(input);
        if (ok) {
            markVerified(email, target);
            redis.delete(key(email));
        } else {
            String ak = attemptKey(email);
            Long n = redis.opsForValue().increment(ak);
            if (n != null && n == 1L) redis.expire(ak, Duration.ofHours(1));
        }
        return ok;
    }

    private void upsertPending(String email, VerifyTarget t, String ip) {
        if (t == VerifyTarget.APPLICANT) {
            var e = applicantRepo.findByEmail(email).orElseGet(() -> {
                var x = new ApplicantEmailVerification();
                x.setEmail(email);
                return x;
            });
            e.setStatus(EmailVerifyStatus.PENDING);
            e.setAttempts(e.getAttempts() == null ? 1 : e.getAttempts() + 1);
            e.setLastRequestedAt(LocalDateTime.now());
            e.setLastIp(ip);
            applicantRepo.save(e);
        } else {
            var e = companyRepo.findByEmail(email).orElseGet(() -> {
                var x = new CompanyEmailVerification();
                x.setEmail(email);
                return x;
            });
            e.setStatus(EmailVerifyStatus.PENDING);
            e.setAttempts(e.getAttempts() == null ? 1 : e.getAttempts() + 1);
            e.setLastRequestedAt(LocalDateTime.now());
            e.setLastIp(ip);
            companyRepo.save(e);
        }
    }

    private void markVerified(String email, VerifyTarget t) {
        if (t == VerifyTarget.APPLICANT) {
            var e = applicantRepo.findByEmail(email).orElseThrow();
            e.setStatus(EmailVerifyStatus.VERIFIED);
            e.setVerifiedAt(LocalDateTime.now());
            applicantRepo.save(e);
        } else {
            var e = companyRepo.findByEmail(email).orElseThrow();
            e.setStatus(EmailVerifyStatus.VERIFIED);
            e.setVerifiedAt(LocalDateTime.now());
            companyRepo.save(e);
        }
    }
}


