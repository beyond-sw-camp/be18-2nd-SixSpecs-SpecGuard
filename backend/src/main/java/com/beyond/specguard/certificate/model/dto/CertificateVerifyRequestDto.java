package com.beyond.specguard.certificate.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CertificateVerifyRequestDto {
    private UUID resumeId;
}
