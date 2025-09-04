package com.beyond.specguard.resume.service;

import com.beyond.specguard.resume.dto.request.ResumeLinkCreateRequest;
import com.beyond.specguard.resume.dto.response.ResumeLinkResponse;
import com.beyond.specguard.resume.dto.request.ResumeLinkUpdateRequest;
import com.beyond.specguard.resume.entity.core.Resume;
import com.beyond.specguard.resume.entity.core.ResumeLink;
import com.beyond.specguard.resume.repository.ResumeLinkRepository;
import com.beyond.specguard.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeLinkService {

    private final ResumeRepository resumeRepository;
    private final ResumeLinkRepository resumeLinkRepository;

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;



    /** 링크 생성 */
    @Transactional
    public ResumeLinkResponse create(String resumeId, ResumeLinkCreateRequest req) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이력서(resumeId) 입니다."));

        ResumeLink saved = resumeLinkRepository.save(
                ResumeLink.builder()
                        .resume(resume)
                        .url(req.url())
                        .linkType(req.linkType())
                        .contents(req.contents())
                        .build()
        );
        return toResponse(saved);
    }

    /** 특정 이력서의 링크 목록 조회 */
    @Transactional(readOnly = true)
    public List<ResumeLinkResponse> list(String resumeId) {
        return resumeLinkRepository.findAllByResumeId(resumeId)
                .stream().map(this::toResponse).toList();
    }

    /** 단건 조회 */
    @Transactional(readOnly = true)
    public ResumeLinkResponse get(String resumeId, String linkId) {
        ResumeLink link = resumeLinkRepository.findByIdAndResumeId(linkId, resumeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 링크가 존재하지 않습니다."));
        return toResponse(link);
    }

    /** 수정(부분 업데이트) */
    @Transactional
    public ResumeLinkResponse update(String resumeId, String linkId, ResumeLinkUpdateRequest req) {
        ResumeLink link = resumeLinkRepository.findByIdAndResumeId(linkId, resumeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 링크가 존재하지 않습니다."));

        ResumeLink updated = ResumeLink.builder()
                .resume(link.getResume())
                .url(req.url() != null ? req.url() : link.getUrl())
                .linkType(req.linkType() != null ? req.linkType() : link.getLinkType())
                .contents(req.contents() != null ? req.contents() : link.getContents())
                .build();

        ResumeLink saved = resumeLinkRepository.save(updated);
        return toResponse(saved);
    }

    /** 삭제 */
    @Transactional
    public void delete(String resumeId, String linkId) {
        if (!resumeLinkRepository.existsByIdAndResumeId(linkId, resumeId)) {
            throw new IllegalArgumentException("해당 링크가 존재하지 않습니다.");
        }
        resumeLinkRepository.deleteByIdAndResumeId(linkId, resumeId);
    }

    /** 엔티티 → 응답 DTO 매핑 */
    private ResumeLinkResponse toResponse(ResumeLink e) {
        return new ResumeLinkResponse(
                e.getId(),
                e.getResume().getId(),
                e.getUrl(),
                e.getLinkType(),
                e.getContents(),
                e.getCreatedAt() != null ? ISO.format(e.getCreatedAt()) : null,
                e.getUpdatedAt() != null ? ISO.format(e.getUpdatedAt()) : null
        );
    }



}
