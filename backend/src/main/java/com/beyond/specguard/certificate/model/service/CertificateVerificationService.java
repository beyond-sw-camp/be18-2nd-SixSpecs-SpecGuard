package com.beyond.specguard.certificate.model.service;


import java.util.UUID;

public interface CertificateVerificationService {
    void verifyCertificateAsync(UUID resumeId);
}
