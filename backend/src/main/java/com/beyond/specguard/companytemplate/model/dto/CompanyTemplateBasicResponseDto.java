package com.beyond.specguard.companytemplate.model.dto;

import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CompanyTemplateBasicResponseDto {
    private UUID id;
    private String name;
    private String department;
    private String category;
    private int yearsOfExperience;
    private LocalDateTime createdAt;

    public static CompanyTemplateBasicResponseDto toDto(CompanyTemplate template) {
        return CompanyTemplateBasicResponseDto.builder()
                .id(template.getId())
                .name(template.getName())
                .yearsOfExperience(template.getYearsOfExperience())
                .category(template.getCategory())
                .department(template.getDepartment())
                .createdAt(template.getCreatedAt())
                .build();
    }
}
