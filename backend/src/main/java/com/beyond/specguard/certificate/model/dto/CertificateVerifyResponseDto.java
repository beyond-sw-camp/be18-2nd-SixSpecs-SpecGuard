package com.beyond.specguard.certificate.model.dto;

import com.beyond.specguard.certificate.model.entity.CertificateVerification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CertificateVerifyResponseDto {
    private UUID certificateId;
    private CertificateVerification.Status status;
    private String message;
}
