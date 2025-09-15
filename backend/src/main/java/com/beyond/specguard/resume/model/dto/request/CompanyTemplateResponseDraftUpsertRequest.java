package com.beyond.specguard.resume.model.dto.request;

import com.beyond.specguard.companytemplate.model.entity.CompanyTemplateField;
import com.beyond.specguard.resume.model.entity.core.CompanyTemplateResponse;
import com.beyond.specguard.resume.model.entity.core.Resume;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@Schema(description = "커스텀 문항 임시저장 요청")
public record CompanyTemplateResponseDraftUpsertRequest(
        @Schema(description = "문항 답변 리스트(빈/NULL 허용)")
        @NotEmpty
        @Valid
        List<Item> responses
) {
    @Schema(description = "문항/답변 아이템")
    public record Item(
            UUID id,

            @Schema(description = "문항 필드 ID (UUID)", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull
            UUID fieldId,

            @Schema(description = "답변(빈문자열/NULL 허용)")
            String answer
    ) {
        public CompanyTemplateResponse toEntity(Resume resume, CompanyTemplateField field) {
            return CompanyTemplateResponse.builder()
                    .companyTemplateField(field)
                    .id(id)
                    .answer(answer == null ? "" : answer)
                    .resume(resume)
                    .build();
        }
    }
}
