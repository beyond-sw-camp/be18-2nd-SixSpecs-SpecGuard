package com.beyond.specguard.resume.model.dto.response;

import com.beyond.specguard.companytemplate.model.dto.response.TemplateFieldResponseDto;
import com.beyond.specguard.resume.model.entity.Resume;
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
        Resume.ResumeStatus status,

        @Schema(description = "성명")
        String name,

        @Schema(description = "연락처")
        String phone,

        @Schema(description = "이메일")
        String email,

        @JsonProperty("basic")
        ResumeBasicResponse resumeBasic,

        @JsonProperty("experiences")
        List<ResumeExperienceResponseDto> resumeExperiences,

        @JsonProperty("links")
        List<ResumeLinkResponseDto> resumeLinks,

        @JsonProperty("educations")
        List<ResumeEducationResponseDto> resumeEducations,

        @JsonProperty("certificates")
        List<ResumeCertificateResponseDto> resumeCertificates,

        @JsonProperty("fields")
        List<TemplateFieldResponseDto> templateFields,

        @JsonProperty("templateResponses")
        List<CompanyTemplateResponseResponse.Item> companyTemplateResponses,

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
                                .resumeBasic(r.getResumeBasic() == null ? null : ResumeBasicResponse.fromEntity(r.getResumeBasic()))
                                .resumeExperiences(r.getResumeExperiences().stream().map(ResumeExperienceResponseDto::fromEntity).toList())
                                .resumeLinks(r.getResumeLinks().stream().map(ResumeLinkResponseDto::fromEntity).toList())
                                .resumeEducations(r.getResumeEducations().stream().map(ResumeEducationResponseDto::fromEntity).toList())
                                .resumeCertificates(r.getResumeCertificates().stream().map(ResumeCertificateResponseDto::fromEntity).toList())
                                .templateFields(r.getTemplate().getFields().stream().map(TemplateFieldResponseDto::fromEntity).toList())
                                .companyTemplateResponses(r.getTemplateResponses().stream().map(CompanyTemplateResponseResponse.Item::fromEntity).toList())
                                .createdAt(r.getCreatedAt())
                                .updatedAt(r.getUpdatedAt())
                                .build();
        }
}
