package com.beyond.specguard.resume.service;

import com.beyond.specguard.resume.dto.request.ResumeCreateRequest;
import com.beyond.specguard.resume.dto.request.ResumeStatusUpdateRequest;
import com.beyond.specguard.resume.dto.request.ResumeUpdateRequest;
import com.beyond.specguard.resume.dto.response.ResumeResponse;
import com.beyond.specguard.resume.entity.core.Resume;
import com.beyond.specguard.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;


    @Transactional
    public ResumeResponse create(ResumeCreateRequest req) {
        Resume saved = resumeRepository.save(
                Resume.builder()
                        .templateId(req.templateId())
                        .name(req.name())
                        .phone(req.phone())
                        .email(req.email())
                        .passwordHash(req.passwordHash())
                        .build()
        );
        return toResponse(saved);
    }


    @Transactional(readOnly = true)
    public ResumeResponse get(UUID id) {
        Resume r = resumeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다: " + id));
        return toResponse(r);
    }


    @Transactional
    public ResumeResponse update(UUID id, ResumeUpdateRequest req) {
        Resume found = resumeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다: " + id));

        if (req.status() != null) found.changeStatus(req.status());
        if (req.name() != null) found.changeName(req.name());
        if (req.phone() != null) found.changePhone(req.phone());
        if (req.email() != null) found.changeEmail(req.email());
        if (req.templateId() != null) found.changeTemplateId(req.templateId());
        if (req.passwordHash() != null) found.changePasswordHash(req.passwordHash());

        return toResponse(found);
    }



    @Transactional
    public ResumeResponse updateStatus(UUID id, ResumeStatusUpdateRequest req) {
        Resume found = resumeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다: " + id));
        found.changeStatus(req.status());
        return toResponse(found);
    }

    @Transactional
    public void delete(UUID id) {
        if (!resumeRepository.existsById(id)) {
            throw new IllegalArgumentException("이력서를 찾을 수 없습니다: " + id);
        }
        resumeRepository.deleteById(id);
    }

    private ResumeResponse toResponse(Resume r) {
        return new ResumeResponse(
                r.getId(),
                r.getTemplateId(),
                r.getStatus(),
                r.getName(),
                r.getPhone(),
                r.getEmail(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
