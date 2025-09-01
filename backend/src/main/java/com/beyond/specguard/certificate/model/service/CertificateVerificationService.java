package com.beyond.specguard.certificate.model.service;

import com.beyond.specguard.certificate.model.entity.ResumeCertificate;


public interface CertificateVerificationService {
    void verifyCertificateAsync(ResumeCertificate resumeCertificate);
}
