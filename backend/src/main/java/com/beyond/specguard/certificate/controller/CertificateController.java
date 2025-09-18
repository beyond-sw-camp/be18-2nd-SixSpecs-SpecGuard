package com.beyond.specguard.certificate.controller;

import com.beyond.specguard.certificate.model.service.CertificateVerificationCodefService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "이력서 자격증 검증 테스트 api", description = "테스트 api 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/verify")
public class CertificateController {

    private final CertificateVerificationCodefService verificationService;

    @PostMapping("/resumes/{resumeId}")
    public ResponseEntity<Void> verify(
            @PathVariable UUID resumeId
    ) {
        verificationService.verifyCertificateAsync(resumeId);

        return ResponseEntity.noContent().build();
    }
}
