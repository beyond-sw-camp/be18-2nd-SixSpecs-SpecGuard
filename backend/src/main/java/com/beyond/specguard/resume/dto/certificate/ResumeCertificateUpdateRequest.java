package com.beyond.specguard.resume.dto.certificate;

import java.time.LocalDate;

public record ResumeCertificateUpdateRequest(
        String certificateName,
        String certificateNumber,
        String issuer,
        LocalDate issuedDate,
        String certUrl
) {
}
