package com.beyond.specguard.companytemplate.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CompanyTemplateDetailRequestDto {
    @Schema(description = "1단계에서 생성된 공고 템플릿 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull(message = "템플릿 ID는 필수 입력값입니다.")
    private UUID templateId;

    @Schema(description = "공고 시작일", example = "2025-09-10T09:00:00")
    @NotNull(message = "공고 시작일은 필수 입력값입니다.")
    @FutureOrPresent(message = "공고 시작일은 현재 시점 이후여야 합니다.")
    private LocalDateTime startDate;

    @Schema(description = "공고 마감일", example = "2025-09-30T18:00:00")
    @NotNull(message = "공고 마감일은 필수 입력값입니다.")
    @Future(message = "공고 마감일은 미래 시점이어야 합니다.")
    private LocalDateTime endDate;

    @Schema(description = "공고에 포함될 자기소개서/추가 문항 리스트")
    @NotEmpty(message = "최소 하나 이상의 필드가 필요합니다.")
    @Valid
    private List<TemplateFieldRequestDto> fields;

}
