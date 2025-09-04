package com.beyond.specguard.resume.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ResumeCreateRequest(

        @NotNull
        String templateId,

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
