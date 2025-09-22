package com.beyond.specguard.resume.model.dto.response;

import com.beyond.specguard.resume.model.entity.ResumeCertificate;
import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record ResumeCertificateResponseDto(
        UUID id,
        String certificateName,
        String certificateNumber,
        String issuer,
        LocalDate issuedDate
) {
    public static ResumeCertificateResponseDto fromEntity(ResumeCertificate resumeCertificate) {
        return ResumeCertificateResponseDto.builder()
                .id(resumeCertificate.getId())
                .certificateName(resumeCertificate.getCertificateName())
                .certificateNumber(resumeCertificate.getCertificateNumber())
                .issuer(resumeCertificate.getIssuer())
                .issuedDate(resumeCertificate.getIssuedDate())
                .build();
    }
}
