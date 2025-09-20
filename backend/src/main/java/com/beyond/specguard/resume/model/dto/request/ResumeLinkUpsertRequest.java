package com.beyond.specguard.resume.model.dto.request;

import com.beyond.specguard.resume.model.entity.Resume;
import com.beyond.specguard.resume.model.entity.ResumeLink;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ResumeLinkUpsertRequest (
        @Schema(description = "링크 항목 ID (없으면 생성)")
        UUID id,

        @Schema(description = "URL", example = "https://github.com/hong123")
        String url,

        @Schema(description = "링크 타입", example = "GITHUB")
        @NotNull
        ResumeLink.LinkType linkType
) {
        public ResumeLink toEntity(Resume resume) {
                return ResumeLink.builder()
                        .resume(resume)
                        .id(id)
                        .linkType(linkType)
                        .url((url == null || url.trim().isEmpty()) ? null : url)
                        .build();
        }
}
