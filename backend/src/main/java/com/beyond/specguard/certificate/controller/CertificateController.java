package com.beyond.specguard.certificate.controller;

import com.beyond.specguard.certificate.model.entity.ResumeCertificate;
import com.beyond.specguard.certificate.model.service.CertificateVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/certificates")
public class CertificateController {

    private final CertificateVerificationService verificationService;

    @PostMapping("/{certificateId}/verify")
    public ResponseEntity<String> verify(@PathVariable UUID certificateId) {
        // certificateId 로 Certificate 조회했다고 가정
        ResumeCertificate certificate = ResumeCertificate.builder()
                        .id(certificateId)
                        .certificateNumber("20260245205321")
                        .certificateName("정보처리기사")
                        .build();

        verificationService.verifyCertificateAsync(certificate);

        return ResponseEntity.ok("Verification started (async)");
    }
}
