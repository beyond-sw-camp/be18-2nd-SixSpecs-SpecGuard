package com.beyond.specguard.resume.model.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class ResumeLoginRequestDto {
    private UUID templateId;
    private String email;
    private String password;
}
