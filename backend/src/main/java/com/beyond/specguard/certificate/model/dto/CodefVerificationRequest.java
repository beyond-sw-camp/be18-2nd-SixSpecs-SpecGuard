package com.beyond.specguard.certificate.model.dto;

import lombok.*;


@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CodefVerificationRequest {
    @Builder.Default
    private String organization = "0001";
    private String userName;
    private String docNo;
}
