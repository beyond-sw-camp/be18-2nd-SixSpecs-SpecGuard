package com.beyond.specguard.resume.model.dto.response;

import com.beyond.specguard.resume.model.entity.common.enums.ResumeStatus;
import com.beyond.specguard.resume.model.entity.core.Resume;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ResumeResponse(
        @Schema(description = "이력서 ID (UUID 문자열)")
        UUID id,

        @Schema(description = "템플릿 ID (UUID 문자열)")
        UUID templateId,

        @Schema(description = "상태")
        ResumeStatus status,

        @Schema(description = "성명")
        String name,

        @Schema(description = "연락처")
        String phone,

        @Schema(description = "이메일")
        String email,

        @Schema(description = "생성 시각")
        LocalDateTime createdAt,

        @Schema(description = "수정 시각")
        LocalDateTime updatedAt
) {
        public static ResumeResponse fromEntity(Resume r) {
                return ResumeResponse.builder()
                                .id(r.getId())
                                .templateId(r.getTemplateId())
                                .status(r.getStatus())
                                .name(r.getName())
                                .phone(r.getPhone())
                                .email(r.getEmail())
                                .createdAt(r.getCreatedAt())
                                .updatedAt(r.getUpdatedAt())
                                .build();
        }
}
