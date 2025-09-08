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
            throw new IllegalArgumentException("요청 헤더 'X-Resume-Secret' 가 필요합니다.");
        }
        Resume r = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다: " + resumeId));
        if (!passwordEncoder.matches(rawSecret, r.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return r;
    }
}