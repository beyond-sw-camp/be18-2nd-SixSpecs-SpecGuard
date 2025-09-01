package com.beyond.specguard.resume.service;

import com.beyond.specguard.resume.dto.resume.request.ResumeCreateRequest;
import com.beyond.specguard.resume.dto.resume.request.ResumeUpdateRequest;
import com.beyond.specguard.resume.dto.resume.response.ResumeListItem;
import com.beyond.specguard.resume.dto.resume.response.ResumeResponse;
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
public class ResumeService {

    private final ResumeRepository resumeRepository;


    @Transactional
    public ResumeResponse create(ResumeCreateRequest req) {
        Resume saved = resumeRepository.save(
                Resume.builder()
                        .templateId(req.templateId())
                        .status(req.status())
                        .name(req.name())
                        .phone(req.phone())
                        .email(req.email())
                        .passwordHash(req.passwordHash())
                        .build()
        );
        return toResponse(saved);
    }


    @Transactional(readOnly = true)
    public ResumeResponse get(String id) {
        Resume r = resumeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다: " + id));
        return toResponse(r);
    }


    @Transactional
    public ResumeResponse update(String id, ResumeUpdateRequest req) {
        Resume found = resumeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다: " + id));


        Resume updated = Resume.builder()
                .id(found.getId())
                .templateId(found.getTemplateId())
                .status(req.status() != null ? req.status() : found.getStatus())
                .name(req.name() != null ? req.name() : found.getName())
                .phone(req.phone() != null ? req.phone() : found.getPhone())
                .email(req.email() != null ? req.email() : found.getEmail())
                .passwordHash(found.getPasswordHash())
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
