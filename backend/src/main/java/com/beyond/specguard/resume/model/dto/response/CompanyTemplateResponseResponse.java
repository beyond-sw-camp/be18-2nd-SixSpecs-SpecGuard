package com.beyond.specguard.resume.model.dto.response;

import com.beyond.specguard.resume.model.entity.core.CompanyTemplateResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record CompanyTemplateResponseResponse(
        @Schema(description = "저장된 개수")
        int savedCount,

        @Schema(description = "저장 결과 목록")
        List<Item> responses
) {
    @Builder
    public record Item(
            UUID id,
            UUID resumeId,
            UUID fieldId,
            String answer,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static Item fromEntity(CompanyTemplateResponse save) {
            return Item.builder()
                    .answer(save.getAnswer())
                    .id(save.getId())
                    .resumeId(save.getResume().getId())
                    .fieldId(save.getCompanyTemplateField().getId())
                    .createdAt(save.getCreatedAt())
                    .updatedAt(save.getUpdatedAt())
                    .build();

        }
    }
}
