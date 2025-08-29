package com.beyond.specguard.certificate.controller;

import com.beyond.specguard.certificate.model.dto.CertificateVerifyRequestDto;
import com.beyond.specguard.certificate.model.entity.ResumeCertificate;
import com.beyond.specguard.certificate.model.service.CertificateVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/certificates")
public class CertificateController {

    private final CertificateVerificationService verificationService;

    @PostMapping("/verify")
    public ResponseEntity<String> verify(
            @RequestBody CertificateVerifyRequestDto requestDto
    ) {
        // certificateId 로 Certificate 조회했다고 가정
        ResumeCertificate certificate = ResumeCertificate.builder()
                        .id(requestDto.getResumeId())
                        .certificateNumber("2025***************")
                        .build();

        verificationService.verifyCertificateAsync(certificate);

        return ResponseEntity.ok("Verification started (async)");
    }
}
