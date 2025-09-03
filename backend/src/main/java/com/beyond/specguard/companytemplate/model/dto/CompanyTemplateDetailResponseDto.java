package com.beyond.specguard.companytemplate.model.dto;

import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplateField;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CompanyTemplateDetailResponseDto {
    private UUID templateId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<TemplateFieldResponseDto> fields;

    public static CompanyTemplateDetailResponseDto toDto(CompanyTemplate template, List<CompanyTemplateField> fields) {
        return CompanyTemplateDetailResponseDto.builder()
                .templateId(template.getId())
                .endDate(template.getEndDate())
                .startDate(template.getStartDate())
                .fields(fields.stream().map(TemplateFieldResponseDto::new).toList())
                .build();

    }
}
