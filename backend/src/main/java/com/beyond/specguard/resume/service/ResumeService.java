package com.beyond.specguard.resume.service;

import com.beyond.specguard.resume.dto.request.ResumeCreateRequest;
import com.beyond.specguard.resume.dto.request.ResumeUpdateRequest;
import com.beyond.specguard.resume.dto.response.ResumeListItem;
import com.beyond.specguard.resume.dto.response.ResumeResponse;
import com.beyond.specguard.resume.entity.common.enums.ResumeStatus;
import com.beyond.specguard.resume.entity.core.Resume;
import com.beyond.specguard.resume.repository.ResumeBasicRepository;
import com.beyond.specguard.resume.repository.ResumeRepository;
import com.beyond.specguard.resume.util.TemplateResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeBasicRepository resumeBasicRepository;
    private final TemplateResolver templateResolver;

    @Transactional
    public ResumeResponse create(ResumeCreateRequest req) {


        String templateId = templateResolver.resolveDefaultTemplateIdForSignUp();

        Resume saved = resumeRepository.save(
                Resume.builder()
                        .templateId(templateId)
                        .status(ResumeStatus.DRAFT)
                        .name(req.name())
                        .phone(req.phone())
                        .email(req.email())
                        .passwordHash(null)
                        .build()
        );
        return toResponse(saved);
}

    public ResumeResponse get(String id) {
        Resume r = resumeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다: " + id));
        return toResponse(r);
    }

    public List<ResumeListItem> list() {
        var resumes = resumeRepository.findAll();
        return resumes.stream().map(r -> {
            var basic = resumeBasicRepository.findByResumeId(r.getId()).orElse(null);
            String applyField = basic != null ? basic.getApplyField() : null;
            String profileImageUrl = basic != null ? basic.getProfileImageUrl() : null;
            return new ResumeListItem(
                    r.getId(), r.getName(), r.getEmail(), r.getStatus(), applyField, profileImageUrl
            );
        }).toList();
    }

    @Transactional
    public ResumeResponse update(String id, ResumeUpdateRequest req) {
        Resume found = resumeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다: " + id));

        // 세터 없이 부분 수정: null이 아닌 값만 빌더로 재조립
        Resume updated = Resume.builder()
                .id(found.getId())                                    // PK 유지
                .templateId(found.getTemplateId())                    // 템플릿 ID 유지
                .status(req.status() != null ? req.status() : found.getStatus()) // 상태 부분 수정
                .name(req.name() != null ? req.name() : found.getName())         // 이름 부분 수정
                .phone(req.phone() != null ? req.phone() : found.getPhone())     // 연락처 부분 수정
                .email(req.email() != null ? req.email() : found.getEmail())     // 이메일 부분 수정
                .passwordHash(found.getPasswordHash())                // 인증 분리 시 그대로 유지
                .build();

        Resume saved = resumeRepository.save(updated);
        return toResponse(saved);
    }

    @Transactional
    public void delete(String id) {
        if (!resumeRepository.existsById(id)) {
            throw new IllegalArgumentException("이력서를 찾을 수 없습니다: " + id);
        }
        resumeRepository.deleteById(id);
    }

    // 엔티티 → 응답 DTO 변환 메서드
    private static ResumeResponse toResponse(Resume r) {
        return new ResumeResponse(
                r.getId(),
                r.getTemplateId(),
                r.getName(),
                r.getPhone(),
                r.getEmail(),
                r.getStatus(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
