package com.beyond.specguard.resume.service;

import com.beyond.specguard.resume.dto.request.ResumeCertificateCreateRequest;
import com.beyond.specguard.resume.dto.response.ResumeCertificateResponse;
import com.beyond.specguard.resume.dto.request.ResumeCertificateUpdateRequest;
import com.beyond.specguard.resume.entity.core.Resume;
import com.beyond.specguard.resume.entity.core.ResumeCertificate;
import com.beyond.specguard.resume.repository.ResumeCertificateRepository;
import com.beyond.specguard.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeCertificateService {

    private final ResumeRepository resumeRepository;
    private final ResumeCertificateRepository resumeCertificateRepository;

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_DATE_TIME;

    /** 생성 */
    @Transactional
    public ResumeCertificateResponse create(String resumeId, ResumeCertificateCreateRequest req) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 resumeId 입니다."));

        ResumeCertificate saved = resumeCertificateRepository.save(
                ResumeCertificate.builder()
                        .resume(resume)
                        .certificateName(req.certificateName())
                        .certificateNumber(req.certificateNumber())
                        .issuer(req.issuer())
                        .issuedDate(req.issuedDate())
                        .certUrl(req.certUrl())
                        .build()
        );
        return toResponse(saved);
    }

    /** 목록 조회 */
    @Transactional(readOnly = true)
    public List<ResumeCertificateResponse> list(String resumeId) {
        return resumeCertificateRepository.findAllByResumeId(resumeId)
                .stream().map(this::toResponse).toList();
    }

    /** 단건 조회 */
    @Transactional(readOnly = true)
    public ResumeCertificateResponse get(String resumeId, String certId) {
        ResumeCertificate cert = resumeCertificateRepository.findByIdAndResumeId(certId, resumeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 자격증이 존재하지 않습니다."));
        return toResponse(cert);
    }

    /** 수정 */
    @Transactional
    public ResumeCertificateResponse update(String resumeId, String certId, ResumeCertificateUpdateRequest req) {
        ResumeCertificate cert = resumeCertificateRepository.findByIdAndResumeId(certId, resumeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 자격증이 존재하지 않습니다."));

        ResumeCertificate updated = ResumeCertificate.builder()
                .id(cert.getId())
                .resume(cert.getResume())
                .certificateName(req.certificateName() != null ? req.certificateName() : cert.getCertificateName())
                .certificateNumber(req.certificateNumber() != null ? req.certificateNumber() : cert.getCertificateNumber())
                .issuer(req.issuer() != null ? req.issuer() : cert.getIssuer())
                .issuedDate(req.issuedDate() != null ? req.issuedDate() : cert.getIssuedDate())
                .certUrl(req.certUrl() != null ? req.certUrl() : cert.getCertUrl())
                .build();

        ResumeCertificate saved = resumeCertificateRepository.save(updated);
        return toResponse(saved);
    }

    /** 삭제 */
    @Transactional
    public void delete(String resumeId, String certId) {
        if (!resumeCertificateRepository.existsByIdAndResumeId(certId, resumeId)) {
            throw new IllegalArgumentException("해당 자격증이 존재하지 않습니다.");
        }
        resumeCertificateRepository.deleteByIdAndResumeId(certId, resumeId);
    }

    /** 엔티티 → 응답 DTO */
    private ResumeCertificateResponse toResponse(ResumeCertificate e) {
        return new ResumeCertificateResponse(
                e.getId(),
                e.getResume().getId(),
                e.getCertificateName(),
                e.getCertificateNumber(),
                e.getIssuer(),
                e.getIssuedDate(),
                e.getCertUrl(),
                e.getCreatedAt() != null ? ISO.format(e.getCreatedAt()) : null,
                e.getUpdatedAt() != null ? ISO.format(e.getUpdatedAt()) : null
        );
    }
}
