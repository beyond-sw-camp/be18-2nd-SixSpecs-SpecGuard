package com.beyond.specguard.resume.dto.resume.request;

import com.beyond.specguard.resume.entity.common.enums.ResumeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ResumeCreateRequest(

        @NotNull
        UUID templateId,

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
