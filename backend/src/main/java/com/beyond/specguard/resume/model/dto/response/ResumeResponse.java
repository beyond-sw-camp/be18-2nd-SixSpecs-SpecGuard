package com.beyond.specguard.resume.model.dto.response;

import com.beyond.specguard.resume.model.entity.common.enums.ResumeStatus;
import com.beyond.specguard.resume.model.entity.core.Resume;
import com.beyond.specguard.resume.model.entity.core.ResumeBasic;
import com.beyond.specguard.resume.model.entity.core.ResumeEducation;
import com.beyond.specguard.resume.model.entity.core.ResumeExperience;
import com.beyond.specguard.resume.model.entity.core.ResumeLink;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
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

        @JsonProperty("basic")
        ResumeBasic resumeBasic,

        @JsonProperty("experiences")
        List<ResumeExperience> resumeExperiences,

        @JsonProperty("links")
        List<ResumeLink> resumeLinks,

        @JsonProperty("educations")
        List<ResumeEducation> resumeEducations,

        @Schema(description = "생성 시각")
        LocalDateTime createdAt,

        @Schema(description = "수정 시각")
        LocalDateTime updatedAt
) {
        public static ResumeResponse fromEntity(Resume r) {
                return ResumeResponse.builder()
                                .id(r.getId())
                                .templateId(r.getTemplate().getId())
                                .status(r.getStatus())
                                .name(r.getName())
                                .phone(r.getPhone())
                                .email(r.getEmail())
                                .resumeBasic(r.getResumeBasic())
                                .resumeExperiences(r.getResumeExperiences())
                                .resumeLinks(r.getResumeLinks())
                                .resumeEducations(r.getResumeEducations())
                                .createdAt(r.getCreatedAt())
                                .updatedAt(r.getUpdatedAt())
                                .build();
        }
}
