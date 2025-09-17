package com.beyond.specguard.verification.model.service;

import com.beyond.specguard.common.config.VerifyConfig;
import com.beyond.specguard.company.common.model.repository.ClientCompanyRepository;
import com.beyond.specguard.resume.model.repository.ResumeRepository;
import com.beyond.specguard.verification.model.entity.ApplicantEmailVerification;
import com.beyond.specguard.verification.model.entity.CompanyEmailVerification;
import com.beyond.specguard.verification.model.repository.ApplicantEmailVerificationRepo;
import com.beyond.specguard.verification.model.repository.CompanyEmailVerificationRepo;
import com.beyond.specguard.verification.model.repository.EmailVerifyRedisRepository;
import com.beyond.specguard.verification.model.type.VerifyTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerifyRedisRepository redisRepo;
    private final VerifyConfig verifyConfig;
    private final VerifySendGridService mailer;

    private final ApplicantEmailVerificationRepo applicantRepo;
    private final CompanyEmailVerificationRepo companyRepo;
    private final ResumeRepository resumeRepository;
    private final ClientCompanyRepository clientCompanyRepository;

    private static String norm(String e) {
        return e == null ? null : e.trim().toLowerCase();
    }

    @Transactional
    public void requestCode(String rawEmail,
                            VerifyTarget target,
                            String ip,
                            @Nullable UUID resumeId,
                            @Nullable UUID companyId) {
        final String email = norm(rawEmail);
        final String code = RandomStringUtils.randomNumeric(6);

        // Redis 저장
        redisRepo.saveCode(email, code);
        log.info("verify.set email={} code(last2)=**{} ttl={}",
                email, code.substring(4), verifyConfig.getTtlSeconds());

        upsertPending(email, target, ip, resumeId, companyId);

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

    private void upsertPending(String email, VerifyTarget t, String ip,
                               @Nullable UUID resumeId, @Nullable UUID companyId) {
        LocalDateTime now = LocalDateTime.now();

        if (t == VerifyTarget.APPLICANT) {
            if (resumeId == null) {
                log.debug("email verify pending (account-scope) email={}", email);
                return;
            }
            var e = applicantRepo.findByEmailAndResumeId(email, resumeId)
                    .orElseGet(() -> ApplicantEmailVerification.forResume(
                            email, resumeRepository.getReferenceById(resumeId)));
            e.markPending(ip, now);
            applicantRepo.save(e);
            return;
        }

        if (t == VerifyTarget.COMPANY) {
            if (companyId == null) {
                var e = companyRepo.findByEmailAndAccountScopeTrue(email)
                        .orElseGet(() -> CompanyEmailVerification.forAccountScope(email));
                e.markPending(ip, now);
                companyRepo.save(e);
                return;
            }
            var e = companyRepo.findByEmailAndCompanyId(email, companyId)
                    .orElseGet(() -> CompanyEmailVerification.forCompany(
                            email, clientCompanyRepository.getReferenceById(companyId)));
            e.markPending(ip, now);
            companyRepo.save(e);
        }
    }

    private void markVerified(String email,
                              VerifyTarget t,
                              @Nullable UUID resumeId,
                              @Nullable UUID companyId) {
        LocalDateTime now = LocalDateTime.now();

        if (t == VerifyTarget.APPLICANT) {
            if (resumeId == null) {
                log.debug("email verified (account-scope) email={}", email);
                return;
            }
            var e = applicantRepo.findByEmailAndResumeId(email, resumeId).orElseThrow();
            e.markVerified(now);
            applicantRepo.save(e);
            return;
        }

        if (t == VerifyTarget.COMPANY) {
            if (companyId == null) {
                var e = companyRepo.findByEmailAndAccountScopeTrue(email)
                        .orElseGet(() -> CompanyEmailVerification.forAccountScope(email));
                e.markVerified(now);
                companyRepo.save(e);
                return;
            }
            var e = companyRepo.findByEmailAndCompanyId(email, companyId).orElseThrow();
            e.markVerified(now);
            companyRepo.save(e);
        }
    }
}
