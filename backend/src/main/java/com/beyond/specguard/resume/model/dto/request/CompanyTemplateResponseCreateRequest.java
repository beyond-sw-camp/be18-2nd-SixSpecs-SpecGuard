package com.beyond.specguard.resume.model.dto.request;

import com.beyond.specguard.companytemplate.model.entity.CompanyTemplateField;
import com.beyond.specguard.resume.model.entity.CompanyTemplateResponse;
import com.beyond.specguard.resume.model.entity.Resume;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CompanyTemplateResponseCreateRequest(
        @Schema(description = "문항 답변 리스트")
        @NotEmpty
        @Valid
        List<Item> responses
) {
    public record Item(
            @Schema(description = "문항 필드 ID (UUID 문자열)")
            @NotNull
            UUID fieldId,

            @Schema(description = "답변(텍스트)")
            @NotBlank
            String answer
    ) {
        public CompanyTemplateResponse toEntity(Resume resume, CompanyTemplateField companyTemplateField) {
            return CompanyTemplateResponse.builder()
                    .id(fieldId)
                    .resume(resume)
                    .companyTemplateField(companyTemplateField)
                    .answer(answer)
                    .build();
        }
    }
}
