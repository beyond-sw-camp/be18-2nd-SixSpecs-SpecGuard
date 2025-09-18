package com.beyond.specguard.certificate.model.service;

import com.beyond.specguard.certificate.model.dto.CodefVerificationRequest;
import com.beyond.specguard.certificate.model.dto.CodefVerificationResponse;
import com.beyond.specguard.certificate.model.entity.CertificateVerification;
import com.beyond.specguard.certificate.model.repository.CertificateVerificationRepository;
import com.beyond.specguard.certificate.util.CertificateNumberUtil;
import com.beyond.specguard.resume.model.entity.ResumeCertificate;
import com.beyond.specguard.resume.model.repository.ResumeCertificateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateVerificationCodefService implements CertificateVerificationService {

    private final CodefClient codefClient;
    private final CertificateVerificationRepository verificationRepository;
    private final ResumeCertificateRepository resumeCertificateRepository;

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void verifyCertificateAsync(UUID resumeId) {
        List<ResumeCertificate> resumeCertificates = resumeCertificateRepository
                .findAllByResumeId(resumeId);

        // 자격증이 없으면 return
        if (resumeCertificates.isEmpty()) {
            return;
        }

        // 자격증 순회
        for (ResumeCertificate certificate : resumeCertificates) {
            CertificateVerification verification = CertificateVerification.builder()
                    .verificationSource("CODEF")
                    .resumeCertificate(certificate)
                    .build();
            try {

                log.debug("name : {}, number : {}", certificate.getResume().getName(), CertificateNumberUtil.preprocessCertificateNumber(certificate.getCertificateNumber()));
                // 요청 DTO 구성
                CodefVerificationRequest request = CodefVerificationRequest.builder()
                        .userName(certificate.getResume().getName())
                        .docNo(CertificateNumberUtil.preprocessCertificateNumber(certificate.getCertificateNumber()))
                        .build();

                // API 호출
                CodefVerificationResponse response = codefClient.verifyCertificate(request);

                verification.setVerifiedNow();

                // 성공 여부 판별
                String resIssueYN = response.getData().getResIssueYN();

                if ("1".equals(resIssueYN)) {
                    verification.setStatusSuccess();
                } else {
                    verification.setStatusFailed();
                    verification.setErrorMessage(response.getData().getResResultDesc());
                }

            } catch (Exception e) {
                verification.setStatusFailed();
                verification.setErrorMessage(e.getMessage());
            } finally {
                verificationRepository.save(verification);
            }
        }
    }
}
