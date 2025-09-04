package com.beyond.specguard.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record ResumeUpdateRequest(

        @Schema(description = "템플릿 ID (UUID 문자열)", example = "8d3d2e72-9a50-40f9-ae51-2a0a3f9f4a43")
        UUID templateId,
        @Schema(description = "성명", example = "홍길동")
        String name,
        @Schema(description = "연락처", example = "010-1234-5678")
        String phone,
        @Schema(description = "이메일", example = "hong123@example.com")
        String email

) {
}
