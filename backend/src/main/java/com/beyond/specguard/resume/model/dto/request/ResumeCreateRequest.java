package com.beyond.specguard.resume.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ResumeCreateRequest(

        @Schema(description = "템플릿 ID (UUID 문자열)", example = "8d3d2e72-9a50-40f9-ae51-2a0a3f9f4a43")
        @NotNull
        UUID templateId,

        @Schema(description = "성명", example = "홍길동")
        @NotBlank
        String name,

        @Schema(description = "연락처", example = "010-1234-5678")
        @NotBlank
        String phone,

        @Schema(description = "이메일", example = "hong123@example.com")
        @Email
        @NotBlank
        String email,

        @Schema(description = "원문 비밀번호(서버에서 해시 저장)")
        @NotBlank
        String password
) {
}
