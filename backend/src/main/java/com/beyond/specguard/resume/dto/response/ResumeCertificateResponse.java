package com.beyond.specguard.resume.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record ResumeCertificateResponse(
        String id,

        String certificateName,

        String certificateNumber,

        String issuer,

        @Schema(type = "string", format = "date")
        LocalDate issuedDate,

        String certUrl
) {
}
