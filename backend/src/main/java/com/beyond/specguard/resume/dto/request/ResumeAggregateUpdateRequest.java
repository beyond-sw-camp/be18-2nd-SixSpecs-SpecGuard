package com.beyond.specguard.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

import java.util.List;

public record ResumeAggregateUpdateRequest(
        @Schema(description = "루트 이력서 일부 필드 변경")
        @Valid
        ResumeCorePatch core,

        @Schema(description = "학력 목록(선택)")
        @Valid
        List<ResumeEducationUpsertRequest> educations,

        @Schema(description = "경력 목록(선택)")
        @Valid
        List<ResumeExperienceUpsertRequest> experiences,

        @Schema(description = "자격증 목록(선택)")
        @Valid
        List<ResumeCertificateUpsertRequest> certificates,

        @Schema(description = "링크 목록(선택)")
        @Valid
        List<ResumeLinkUpsertRequest> links
) {
}
