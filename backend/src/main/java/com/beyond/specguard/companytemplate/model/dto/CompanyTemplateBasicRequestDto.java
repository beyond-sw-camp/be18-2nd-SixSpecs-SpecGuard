package com.beyond.specguard.companytemplate.model.dto;

import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyTemplateBasicRequestDto {
    private String name;
    private String description;
    private String department;
    private String category;
    private int yearsOfExperience;

    public CompanyTemplate toDto() {
        return CompanyTemplate.builder()
                .companyId("WIP")
                .name(name)
                .description(description)
                .department(department)
                .category(category)
                .yearsOfExperience(yearsOfExperience)
                .endDate(LocalDateTime.now().plusDays(7))
                .build();
    }
}
