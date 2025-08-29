package com.beyond.specguard.certificate.model.repository;

import com.beyond.specguard.certificate.model.entity.CertificateVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateVerificationRepository extends JpaRepository<CertificateVerification, String> {
}
