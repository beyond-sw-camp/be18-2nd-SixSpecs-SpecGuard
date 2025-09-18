package com.beyond.specguard.certificate.model.dto;

import com.beyond.specguard.certificate.model.entity.CertificateVerification;
import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CertificateVerifyResponseDto {
    private List<CertificateVerification> certificateVerifications;
}
