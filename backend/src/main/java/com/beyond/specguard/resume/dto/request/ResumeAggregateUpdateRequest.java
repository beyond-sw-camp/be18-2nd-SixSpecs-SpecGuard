package com.beyond.specguard.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

public record ResumeAggregateUpdateRequest(
        @Schema(description = "루트 이력서 일부 필드 변경")
        @Valid
        ResumeCorePatch core,

        @Schema(description = "학력 목록(선택)")
        @Valid
        List<EducationUpsertRequest> educations,

        @Schema(description = "경력 목록(선택)")
        @Valid
        List<ExperienceUpsertRequest> experiences,

        @Schema(description = "자격증 목록(선택)")
        @Valid
        List<CertificateUpsertRequest> certificates,

        @Schema(description = "링크 목록(선택)")
        @Valid
        List<LinkUpsertRequest> links
) {
}
