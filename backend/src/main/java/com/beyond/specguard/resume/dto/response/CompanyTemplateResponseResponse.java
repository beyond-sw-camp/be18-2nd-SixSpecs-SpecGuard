package com.beyond.specguard.resume.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CompanyTemplateResponseResponse(
        @Schema(description = "저장된 개수")
        int savedCount,

        @Schema(description = "저장 결과 목록")
        List<Item> responses
) {
    public record Item(
            UUID id,
            UUID resumeId,
            UUID fieldId,
            String answer,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
}
