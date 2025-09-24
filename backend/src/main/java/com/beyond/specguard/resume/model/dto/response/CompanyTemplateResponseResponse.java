package com.beyond.specguard.resume.model.dto.response;

import com.beyond.specguard.resume.model.entity.CompanyTemplateResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

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
            UUID fieldId,
            String answer
    ) {
        public static Item fromEntity(CompanyTemplateResponse save) {
            return Item.builder()
                    .answer(save.getAnswer())
                    .id(save.getId())
                    .fieldId(save.getCompanyTemplateField().getId())
                    .build();

        }
    }
}
