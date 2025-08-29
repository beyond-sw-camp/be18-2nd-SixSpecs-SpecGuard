package com.beyond.specguard.certificate.model.service;

import com.beyond.specguard.certificate.model.dto.CodefVerificationRequest;
import com.beyond.specguard.certificate.model.dto.CodefVerificationResponse;
import com.beyond.specguard.certificate.model.entity.CertificateVerification;
import com.beyond.specguard.certificate.model.entity.ResumeCertificate;
import com.beyond.specguard.certificate.model.repository.CertificateVerificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateVerificationService {

    private final CodefClient codefClient;
    private final CertificateVerificationRepository verificationRepository;

    @Async
    @Transactional
    public void verifyCertificateAsync(ResumeCertificate certificate) {
        CertificateVerification verification = CertificateVerification.builder()
                        .verificationSource("CODEF")
                        .resumeCertificate(certificate)
                        .build();

        verificationRepository.save(verification);

        try {
            // 요청 DTO 구성
            CodefVerificationRequest request = CodefVerificationRequest.builder()
                    .organization("0001")
                    .userName("***")
                    .docNo(certificate.getCertificateNumber())
                    .build();

            // API 호출
            CodefVerificationResponse response = codefClient.verifyCertificate(request);

            log.debug(response.toString());

            verification.setVerifiedAt(LocalDateTime.now());

            // 성공 여부 판별
            String resIssueYN = response.getData().getResIssueYN();
            if ("1".equals(resIssueYN)) {
                verification.setStatus(CertificateVerification.Status.SUCCESS);
            } else {
                verification.setStatus(CertificateVerification.Status.FAILED);
                verification.setErrorMessage(response.getData().getResResultDesc());
            }

        } catch (Exception e) {
            verification.setStatus(CertificateVerification.Status.FAILED);
            verification.setErrorMessage(e.getMessage());
        }

        verificationRepository.save(verification);
    }
}
