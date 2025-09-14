package com.beyond.specguard.certificate.model.service;


import com.beyond.specguard.certificate.model.dto.CertificateVerifyResponseDto;

import java.util.UUID;

public interface CertificateVerificationService {
    void verifyCertificateAsync(UUID resumeId);

    CertificateVerifyResponseDto getCertificateVerifications(UUID resumeId);
}
