package com.beyond.specguard.verification.model.service;

import com.beyond.specguard.resume.model.repository.ResumeRepository;
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
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static io.lettuce.core.KillArgs.Builder.id;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final StringRedisTemplate redis;
    private final VerifySendGridService mailer;

    private final ApplicantEmailVerificationRepo applicantRepo;
    private final CompanyEmailVerificationRepo companyRepo;
    private final ResumeRepository resumeRepository;

    @Value("${verify.ttl-seconds:300}")
    private long ttlSeconds;

    private static String norm(String e){ return e == null ? null : e.trim().toLowerCase(); }
    private String key(String email) { return "verif:email:" + email.toLowerCase(); }
    private String attemptKey(String email) { return "verif:attempt:" + email.toLowerCase(); }

    @Transactional
    public void requestCode(String rawEmail, VerifyTarget target, String ip, @Nullable UUID resumeId) {
        final String email = norm(rawEmail);
        final String code  = RandomStringUtils.randomNumeric(6);
        final String k = key(email);

        redis.opsForValue().set(k, code, Duration.ofSeconds(ttlSeconds));
        log.info("verify.set key={} code(last2)=**{} ttl={}", k, code.substring(4), ttlSeconds);

        upsertPending(rawEmail, target, ip, resumeId);
        mailer.sendCodeEmail(email, code, ttlSeconds); // unchecked 예외 전파


    }

    @Transactional
    public boolean verify(String rawEmail, String input, VerifyTarget target, @Nullable UUID resumeId) {
        final String email = norm(rawEmail);
        final String k = key(email);
        String saved = redis.opsForValue().get(k);
        if (saved == null) return false;

        String in = input == null ? "" : input.trim().replaceAll("\\D", "");
        boolean ok = saved.equals(in);
        if (ok) {
            markVerified(email, target, resumeId);
            redis.delete(k);
        } else {
            String ak = attemptKey(email);
            Long n = redis.opsForValue().increment(ak);
            if (n != null && n == 1L) redis.expire(ak, Duration.ofHours(1));
        }
        return ok;
    }

    private void upsertPending(String rawEmail, VerifyTarget t, String ip, @Nullable UUID resumeId) {
        final String email = norm(rawEmail);
        if (t == VerifyTarget.APPLICANT) {
            if (resumeId == null) throw new IllegalArgumentException("resumeId required for applicant");
            var e = applicantRepo.findByEmailAndResumeId(email, resumeId).orElseGet(() -> {
                var x = new ApplicantEmailVerification();
                x.setEmail(email);
                x.setResume(resumeRepository.getReferenceById(resumeId));
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

    private void markVerified(String rawEmail, VerifyTarget t, @Nullable UUID resumeId) {
        final String email = norm(rawEmail);
        if (t == VerifyTarget.APPLICANT) {
            var e = applicantRepo.findByEmailAndResumeId(email, resumeId).orElseThrow();
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


