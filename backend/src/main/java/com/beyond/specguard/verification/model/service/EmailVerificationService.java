package com.beyond.specguard.verification.model.service;

import com.beyond.specguard.common.config.VerifyConfig;
import com.beyond.specguard.company.common.model.repository.ClientCompanyRepository;
import com.beyond.specguard.resume.model.repository.ResumeRepository;
import com.beyond.specguard.verification.model.entity.ApplicantEmailVerification;
import com.beyond.specguard.verification.model.entity.CompanyEmailVerification;
import com.beyond.specguard.verification.model.entity.EmailVerifyStatus;
import com.beyond.specguard.verification.model.repository.ApplicantEmailVerificationRepo;
import com.beyond.specguard.verification.model.repository.CompanyEmailVerificationRepo;
import com.beyond.specguard.verification.model.repository.EmailVerifyRedisRepository;
import com.beyond.specguard.verification.model.type.VerifyTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

//    @Qualifier("verifyCodeTtlSeconds")
//    private final long codeTtlSeconds;
    private final EmailVerifyRedisRepository redisRepo;
    private final VerifyConfig verifyConfig;
    private final VerifySendGridService mailer;

    private final ApplicantEmailVerificationRepo applicantRepo;
    private final CompanyEmailVerificationRepo companyRepo;
    private final ResumeRepository resumeRepository;
    private final ClientCompanyRepository clientCompanyRepository;

    private static String norm(String e){ return e == null ? null : e.trim().toLowerCase(); }

    @Transactional
    public void requestCode(String rawEmail,
                            VerifyTarget target,
                            String ip,
                            @Nullable UUID resumeId,
                            @Nullable UUID companyId) {
        final String email = norm(rawEmail);
        final String code  = RandomStringUtils.randomNumeric(6);

        // Redis 저장(키/TTL 캡슐화)
        redisRepo.saveCode(email, code);
        log.info("verify.set email={} code(last2)=**{} ttl={}",
                email, code.substring(4), verifyConfig.getTtlSeconds());

        upsertPending(rawEmail, target, ip, resumeId, companyId);

        mailer.sendCodeEmail(email, code, verifyConfig.getTtlSeconds());
    }

    @Transactional
    public boolean verify(String rawEmail,
                          String input,
                          VerifyTarget target,
                          @Nullable UUID resumeId,
                          @Nullable UUID companyId) {
        final String email = norm(rawEmail);
        String saved = redisRepo.getCode(email);
        if (saved == null) return false;

        String in = input == null ? "" : input.trim().replaceAll("\\D", "");
        boolean ok = saved.equals(in);
        if (ok) {
            markVerified(email, target, resumeId, companyId);
            redisRepo.deleteCode(email);
        } else {
            redisRepo.incrAttempt(email);
        }
        return ok;
    }

    private void upsertPending(String rawEmail,
                               VerifyTarget t,
                               String ip,
                               @Nullable UUID resumeId,
                               @Nullable UUID companyId) {
        final String email = norm(rawEmail);
        if (t == VerifyTarget.APPLICANT) {
            if (resumeId == null) {  // 가입 단계: DB 미저장 OK(요구사항)
                log.debug("email verify pending (account-scope) email={}", email);
                return;
            }
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
            return;
        }
        if (t == VerifyTarget.COMPANY) {
            if (companyId == null) { // ✅ account-scope upsert
                var e = companyRepo.findByEmailAndAccountScopeTrue(email).orElseGet(() -> {
                    var x = new CompanyEmailVerification();
                    x.setEmail(email);
                    x.setAccountScope(true);
                    return x;
                });
                e.setStatus(EmailVerifyStatus.PENDING);
                e.setAttempts(e.getAttempts() == null ? 1 : e.getAttempts() + 1);
                e.setLastRequestedAt(LocalDateTime.now());
                e.setLastIp(ip);
                companyRepo.save(e);
                return;
            }
            var e = companyRepo.findByEmailAndCompanyId(email, companyId).orElseGet(() -> {
                var x = new CompanyEmailVerification();
                x.setEmail(email);
                x.setCompany(clientCompanyRepository.getReferenceById(companyId));
                return x;
            });
            e.setStatus(EmailVerifyStatus.PENDING);
            e.setAttempts(e.getAttempts() == null ? 1 : e.getAttempts() + 1);
            e.setLastRequestedAt(LocalDateTime.now());
            e.setLastIp(ip);
            companyRepo.save(e);
        }
    }

    private void markVerified(String email,
                              VerifyTarget t,
                              @Nullable UUID resumeId,
                              @Nullable UUID companyId) {
        if (t == VerifyTarget.APPLICANT) {
            if (resumeId == null) {
                log.debug("email verified (account-scope) email={}", email);
                return;
            }
            var e = applicantRepo.findByEmailAndResumeId(email, resumeId).orElseThrow();
            e.setStatus(EmailVerifyStatus.VERIFIED);
            e.setVerifiedAt(LocalDateTime.now());
            applicantRepo.save(e);
            return;
        }
        if (t == VerifyTarget.COMPANY) {
            if (companyId == null) { // ✅ account-scope update
                var e = companyRepo.findByEmailAndAccountScopeTrue(email).orElseGet(() -> {
                    var x = new CompanyEmailVerification();
                    x.setEmail(email);
                    x.setAccountScope(true);
                    x.setStatus(EmailVerifyStatus.PENDING);
                    return x;
                });
                e.setStatus(EmailVerifyStatus.VERIFIED);
                e.setVerifiedAt(LocalDateTime.now());
                companyRepo.save(e);
                return;
            }
            var e = companyRepo.findByEmailAndCompanyId(email, companyId).orElseThrow();
            e.setStatus(EmailVerifyStatus.VERIFIED);
            e.setVerifiedAt(LocalDateTime.now());
            companyRepo.save(e);
        }
    }
}



