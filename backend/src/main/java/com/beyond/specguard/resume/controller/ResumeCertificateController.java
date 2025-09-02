package com.beyond.specguard.resume.controller;

import com.beyond.specguard.resume.dto.certificate.ResumeCertificateCreateRequest;
import com.beyond.specguard.resume.dto.certificate.ResumeCertificateResponse;
import com.beyond.specguard.resume.dto.certificate.ResumeCertificateUpdateRequest;
import com.beyond.specguard.resume.service.ResumeCertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resumes/{resumeId}/certificates")
public class ResumeCertificateController {

    private final ResumeCertificateService resumeCertificateService;

    /** 생성 POST /api/v1/resumes/{resumeId}/certificates */
    @PostMapping
    public ResponseEntity<ResumeCertificateResponse> create(
            @PathVariable String resumeId,
            @RequestBody ResumeCertificateCreateRequest req
    ) {
        ResumeCertificateResponse res = resumeCertificateService.create(resumeId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /** 목록 GET /api/v1/resumes/{resumeId}/certificates */
    @GetMapping
    public ResponseEntity<List<ResumeCertificateResponse>> list(@PathVariable String resumeId) {
        return ResponseEntity.ok(resumeCertificateService.list(resumeId));
    }

    /** 단건 GET /api/v1/resumes/{resumeId}/certificates/{certId} */
    @GetMapping("/{certId}")
    public ResponseEntity<ResumeCertificateResponse> get(
            @PathVariable String resumeId,
            @PathVariable String certId
    ) {
        return ResponseEntity.ok(resumeCertificateService.get(resumeId, certId));
    }

    /** 수정 PATCH /api/v1/resumes/{resumeId}/certificates/{certId} */
    @PatchMapping("/{certId}")
    public ResponseEntity<ResumeCertificateResponse> update(
            @PathVariable String resumeId,
            @PathVariable String certId,
            @RequestBody ResumeCertificateUpdateRequest req
    ) {
        return ResponseEntity.ok(resumeCertificateService.update(resumeId, certId, req));
    }

    /** 삭제 DELETE /api/v1/resumes/{resumeId}/certificates/{certId} */
    @DeleteMapping("/{certId}")
    public ResponseEntity<Void> delete(
            @PathVariable String resumeId,
            @PathVariable String certId
    ) {
        resumeCertificateService.delete(resumeId, certId);
        return ResponseEntity.noContent().build();
    }
}