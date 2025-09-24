package com.beyond.specguard.certificate.model.repository;

import com.beyond.specguard.certificate.model.entity.CertificateVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CertificateVerificationRepository extends JpaRepository<CertificateVerification, String> {

    // 여러 자격증의 최신 검증 결과를 한 번에 가져오기
    @Query("SELECT cv FROM CertificateVerification cv " +
            "WHERE cv.resumeCertificate.id IN :certificateIds " +
            "AND cv.verifiedAt = (SELECT MAX(subCv.verifiedAt) FROM CertificateVerification subCv WHERE subCv.resumeCertificate.id = cv.resumeCertificate.id)")
    List<CertificateVerification> findLatestByCertificateIds(List<UUID> certificateIds);
}
