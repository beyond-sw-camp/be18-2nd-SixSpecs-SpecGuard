package com.beyond.specguard.certificate.model.dto;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class CodefVerificationRequest {
    private String organization;
    private String userName;
    private String docNo;
}
