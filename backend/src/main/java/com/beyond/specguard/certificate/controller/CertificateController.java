package com.beyond.specguard.certificate.controller;

import com.beyond.specguard.certificate.model.dto.CertificateVerifyResponseDto;
import com.beyond.specguard.certificate.model.entity.CertificateVerification;
import com.beyond.specguard.certificate.model.entity.ResumeCertificate;
import com.beyond.specguard.certificate.model.service.CertificateVerificationCodefService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resumes")
public class CertificateController {

    private final CertificateVerificationCodefService verificationService;

    @PostMapping("/{resumeId}/certificate/{certificateId}/verify")
    public ResponseEntity<CertificateVerifyResponseDto> verify(
            @PathVariable UUID resumeId,
            @PathVariable UUID certificateId
    ) {
        // certificateId 로 Certificate 조회했다고 가정
        // TODO: ResumeCertificate Repository 에서 조회기능 추가
        ResumeCertificate certificate = ResumeCertificate.builder()
                        .id(resumeId)
                        .certificateNumber("2025***************")
                        .build();

        verificationService.verifyCertificateAsync(certificate);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(
                new CertificateVerifyResponseDto(
                        certificateId,
                        CertificateVerification.Status.PENDING,
                        "Verification started"
                )
        );
    }
}
