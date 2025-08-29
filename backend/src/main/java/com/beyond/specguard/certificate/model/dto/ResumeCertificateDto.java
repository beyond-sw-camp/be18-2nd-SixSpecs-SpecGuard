package com.beyond.specguard.certificate.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResumeCertificateDto {
    private String id;
    private String certificateName;
    private String certificateNumber;
    private String issuer;
    private LocalDate issuedDate;
    private String certUrl;
}
