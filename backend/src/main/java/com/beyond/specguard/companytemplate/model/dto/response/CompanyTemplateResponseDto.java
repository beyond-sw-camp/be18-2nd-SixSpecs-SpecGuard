package com.beyond.specguard.companytemplate.model.dto.response;

import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplateField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyTemplateResponseDto {
    @JsonProperty("basic")
    private BasicDto basicDto;

    @JsonProperty("detail")
    private DetailDto detailDto;

    public CompanyTemplateResponseDto(CompanyTemplate companyTemplate, List<CompanyTemplateField> companyTemplateFields) {
        this.basicDto = BasicDto.toDto(companyTemplate);
        this.detailDto = DetailDto.toDto(companyTemplate, companyTemplateFields);
    }


    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class BasicDto {
        private UUID id;
        private String name;
        private String description;
        private String department;
        private String category;
        private int yearsOfExperience;
        private LocalDateTime createdAt;

        public static BasicDto toDto(CompanyTemplate template) {
            return BasicDto.builder()
                    .id(template.getId())
                    .name(template.getName())
                    .description(template.getDescription())
                    .yearsOfExperience(template.getYearsOfExperience())
                    .category(template.getCategory())
                    .department(template.getDepartment())
                    .createdAt(template.getCreatedAt())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class DetailDto {
        private UUID templateId;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private List<TemplateFieldResponseDto> fields;

        public static DetailDto toDto(CompanyTemplate template, List<CompanyTemplateField> fields) {
            return DetailDto.builder()
                    .templateId(template.getId())
                    .endDate(template.getEndDate())
                    .startDate(template.getStartDate())
                    .fields(fields.stream().map(TemplateFieldResponseDto::new).toList())
                    .build();

        }
    }
}
