package com.beyond.specguard.resume.dto.request;

import com.beyond.specguard.resume.entity.common.enums.ResumeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ResumeCreateRequest(

        @NotBlank
        String name,

        @NotBlank
        String phone,

        @Email
        @NotBlank
        String email
) {
}
