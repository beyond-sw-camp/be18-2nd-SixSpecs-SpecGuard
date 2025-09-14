package com.beyond.specguard.applicant.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class ApplicantLoginRequestDto {
    private UUID templateId;
    private String email;
    private String password;
}
