package com.beyond.specguard.resume.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

public record ResumeCertificateResponse(
        UUID id,

        String certificateName,

        String certificateNumber,

        String issuer,

        @Schema(type = "string", format = "date")
        LocalDate issuedDate,

        String certUrl
) {
}
