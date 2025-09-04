package com.beyond.specguard.resume.dto.request;

import java.time.LocalDate;

public record ResumeCertificateCreateRequest(
        String certificateName,
        String certificateNumber,
        String issuer,
        LocalDate issuedDate,
        String certUrl
) {
}
