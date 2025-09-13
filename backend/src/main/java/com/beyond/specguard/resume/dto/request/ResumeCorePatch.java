package com.beyond.specguard.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

import java.util.UUID;

public record ResumeCorePatch(
        @Schema(description = "템플릿 ID (UUID 문자열)")
        UUID templateId,

        @Schema(description = "성명")
        String name,

        @Schema(description = "연락처")
        String phone,

        @Schema(description = "이메일")
        @Email
        String email
) {
}
