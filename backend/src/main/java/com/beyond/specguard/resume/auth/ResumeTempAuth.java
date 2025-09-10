package com.beyond.specguard.resume.auth;

import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.resume.entity.core.Resume;
import com.beyond.specguard.resume.exception.errorcode.ResumeErrorCode;
import com.beyond.specguard.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ResumeTempAuth {
    private final ResumeRepository resumeRepository;
    private final PasswordEncoder passwordEncoder;

    /** X-Resume-Id + X-Resume-Secret 로 후보자 인증 */
    public Resume authenticate(UUID resumeId, String rawSecret) {
        if (rawSecret == null || rawSecret.isBlank()) {
            throw new CustomException(ResumeErrorCode.INVALID_REQUEST);
        }
        Resume r = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new CustomException(ResumeErrorCode.RESUME_NOT_FOUND));
        if (!passwordEncoder.matches(rawSecret, r.getPasswordHash())) {
            throw new CustomException(ResumeErrorCode.INVALID_RESUME_CREDENTIAL);
        }
        return r;
    }
}