package com.beyond.specguard.resume.dto.resume.request;

import com.beyond.specguard.resume.entity.common.enums.ResumeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResumeCreateRequest(

        @NotBlank
        String templateId,

        @NotBlank
        ResumeStatus status,

        @NotBlank
        String name,

        @NotBlank
        String phone,

        @Email
        @NotBlank
        String email,

        @NotBlank
        String passwordHash
) {
}
