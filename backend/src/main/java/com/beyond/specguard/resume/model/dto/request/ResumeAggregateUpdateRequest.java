package com.beyond.specguard.resume.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

import java.util.List;

public record ResumeAggregateUpdateRequest(
        @Schema(description = "학력 목록")
        @Valid
        List<ResumeEducationUpsertRequest> educations,

        @Schema(description = "경력 목록")
        @Valid
        List<ResumeExperienceUpsertRequest> experiences,

        @Schema(description = "자격증 목록")
        @Valid
        List<ResumeCertificateUpsertRequest> certificates,

        @Schema(description = "링크 목록")
        @Valid
        List<ResumeLinkUpsertRequest> links
) {
}
