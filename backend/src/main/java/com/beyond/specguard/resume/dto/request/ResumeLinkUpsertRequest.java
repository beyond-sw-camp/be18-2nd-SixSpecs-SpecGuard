package com.beyond.specguard.resume.dto.request;

import com.beyond.specguard.resume.entity.common.enums.LinkType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record ResumeLinkUpsertRequest(
        @Schema(description = "링크 항목 ID (없으면 생성)")
        UUID id,

        @Schema(description = "URL", example = "https://github.com/hong123")
        @NotBlank
        String url,

        @Schema(description = "레이블/설명", example = "GitHub")
        String label
) {
}
