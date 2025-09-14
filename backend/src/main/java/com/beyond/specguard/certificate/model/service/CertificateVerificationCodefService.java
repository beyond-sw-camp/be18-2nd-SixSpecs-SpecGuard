package com.beyond.specguard.certificate.model.service;

import com.beyond.specguard.certificate.model.dto.CertificateVerifyResponseDto;
import com.beyond.specguard.certificate.model.dto.CodefVerificationRequest;
import com.beyond.specguard.certificate.model.dto.CodefVerificationResponse;
import com.beyond.specguard.certificate.model.entity.CertificateVerification;
import com.beyond.specguard.certificate.model.repository.CertificateVerificationRepository;
import com.beyond.specguard.resume.model.entity.core.ResumeCertificate;
import com.beyond.specguard.resume.model.repository.ResumeCertificateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateVerificationCodefService implements CertificateVerificationService {

    private final CodefClient codefClient;
    private final CertificateVerificationRepository verificationRepository;
    private final ResumeCertificateRepository resumeCertificateRepository;

    @Override
    @Async
    @Transactional
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

                // 요청 DTO 구성
                CodefVerificationRequest request = CodefVerificationRequest.builder()
                        .userName(certificate.getResume().getName())
                        .docNo(certificate.getCertificateNumber())
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
            }

            verificationRepository.save(verification);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CertificateVerifyResponseDto getCertificateVerifications(UUID resumeId) {

        List<ResumeCertificate> certificates = resumeCertificateRepository.findAllByResumeId(resumeId);

        if (certificates.isEmpty()) {
            return CertificateVerifyResponseDto.builder().build();
        }

        List<UUID> certificateIds = certificates.stream()
                .map(ResumeCertificate::getId)
                .collect(Collectors.toList());

        List<CertificateVerification> latestVerifications = verificationRepository.findLatestByCertificateIds(certificateIds);

        return CertificateVerifyResponseDto.builder()
                .certificateVerifications(latestVerifications)
                .build();
    }
}
