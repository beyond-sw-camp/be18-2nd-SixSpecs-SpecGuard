package com.beyond.specguard.resume.service;

import com.beyond.specguard.resume.dto.templateResponse.CompanyTemplateResponseCreateRequest;
import com.beyond.specguard.resume.dto.templateResponse.CompanyTemplateResponseResponse;
import com.beyond.specguard.resume.dto.templateResponse.CompanyTemplateResponseUpdateRequest;
import com.beyond.specguard.resume.entity.core.CompanyTemplateResponse;
import com.beyond.specguard.resume.entity.core.Resume;
import com.beyond.specguard.resume.repository.CompanyTemplateResponseRepository;
import com.beyond.specguard.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyTemplateResponseService {

    private final ResumeRepository resumeRepository;
    private final CompanyTemplateResponseRepository repository;

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_DATE_TIME;

    /** 생성 */
    @Transactional
    public CompanyTemplateResponseResponse create(String resumeId, CompanyTemplateResponseCreateRequest req) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 resumeId 입니다."));

        if (repository.existsByResumeIdAndFieldId(resumeId, req.fieldId())) {
            throw new IllegalStateException("이미 해당 질문에 대한 답변이 존재합니다. PATCH API를 이용하세요.");
        }

        var saved = repository.save(
                CompanyTemplateResponse.builder()
                        .resume(resume)
                        .fieldId(req.fieldId())
                        .answer(req.answer())
                        .build()
        );
        return toResponse(saved);
    }

    /** 목록 조회 */
    @Transactional(readOnly = true)
    public List<CompanyTemplateResponseResponse> list(String resumeId) {
        return repository.findAllByResumeId(resumeId)
                .stream().map(this::toResponse).toList();
    }

    /** 단건 조회 */
    @Transactional(readOnly = true)
    public CompanyTemplateResponseResponse get(String resumeId, String respId) {
        var resp = repository.findByIdAndResumeId(respId, resumeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 답변이 존재하지 않습니다."));
        return toResponse(resp);
    }

    /** 수정 */
    @Transactional
    public CompanyTemplateResponseResponse update(String resumeId, String respId, CompanyTemplateResponseUpdateRequest req) {
        var cur = repository.findByIdAndResumeId(respId, resumeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 답변이 존재하지 않습니다."));

        var updated = CompanyTemplateResponse.builder()
                .id(cur.getId())
                .resume(cur.getResume())
                .fieldId(cur.getFieldId())
                .answer(req.answer() != null ? req.answer() : cur.getAnswer())
                .build();

        var saved = repository.save(updated);
        return toResponse(saved);
    }

    /** 삭제 */
    @Transactional
    public void delete(String resumeId, String respId) {
        repository.deleteByIdAndResumeId(respId, resumeId);
    }

    private CompanyTemplateResponseResponse toResponse(CompanyTemplateResponse e) {
        return new CompanyTemplateResponseResponse(
                e.getId(),
                e.getResume().getId(),
                e.getFieldId(),
                e.getAnswer(),
                e.getCreatedAt() != null ? ISO.format(e.getCreatedAt()) : null,
                e.getUpdatedAt() != null ? ISO.format(e.getUpdatedAt()) : null
        );
    }
}