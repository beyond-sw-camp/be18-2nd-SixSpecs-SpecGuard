package com.beyond.specguard.resume.dto.response;

import java.time.LocalDate;

public record ResumeCertificateResponse(
        String id,
        String resumeId,
        String certificateName,
        String certificateNumber,
        String issuer,
        LocalDate issuedDate,
        String certUrl,
        String createdAt,
        String updatedAt
) {
}
