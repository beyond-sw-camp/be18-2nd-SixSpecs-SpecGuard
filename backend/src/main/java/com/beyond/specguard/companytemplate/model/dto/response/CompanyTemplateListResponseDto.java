package com.beyond.specguard.companytemplate.model.dto.response;

import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyTemplateListResponseDto {
    private UUID id;
    private String name;
    private String department;
    private String category;
    private Integer yearsOfExperience;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private CompanyTemplate.TemplateStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CompanyTemplateListResponseDto fromEntity(CompanyTemplate template) {
        return CompanyTemplateListResponseDto.builder()
                .id(template.getId())
                .name(template.getName())
                .department(template.getDepartment())
                .category(template.getCategory())
                .yearsOfExperience(template.getYearsOfExperience())
                .startDate(template.getStartDate())
                .endDate(template.getEndDate())
                .status(template.getStatus())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
