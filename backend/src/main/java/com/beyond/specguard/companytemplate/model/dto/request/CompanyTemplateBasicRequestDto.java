package com.beyond.specguard.companytemplate.model.dto.request;

import com.beyond.specguard.common.validation.Create;
import com.beyond.specguard.company.common.model.entity.ClientCompany;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class CompanyTemplateBasicRequestDto {
    @Schema(description = "공고명", example = "2025년 상반기 신입 개발자 채용")
    @NotBlank(groups = Create.class, message = "공고명은 필수 입력값입니다.")
    @Size(max = 50, message = "공고명은 최대 50자까지 입력 가능합니다.")
    private String name;

    @Schema(description = "공고 설명", example = "백엔드 및 프론트엔드 개발자를 모집합니다.")
    @Size(max = 1000, message = "공고 설명은 최대 1000자까지 입력 가능합니다.")
    private String description;

    @Schema(description = "부서명", example = "플랫폼개발팀")
    @NotBlank(groups = Create.class, message = "부서는 필수 입력값입니다.")
    @Size(max = 100, message = "부서는 최대 100자까지 입력 가능합니다.")
    private String department;

    @Schema(description = "직무 카테고리", example = "백엔드 개발")
    @NotBlank(groups = Create.class, message = "직무는 필수 입력값입니다.")
    @Size(max = 100, message = "직무는 최대 100자까지 입력 가능합니다.")
    private String category;

    @Schema(description = "필요 경력(년차)", example = "3")
    @NotNull(groups = Create.class, message = "연차 정보는 필수 입력값입니다.")
    @Min(value = 0, message = "연차는 0 이상이어야 합니다.")
    @Max(value = 50, message = "연차는 50 이하여야 합니다.") // 현실적인 upper bound
    private Integer yearsOfExperience;

    public CompanyTemplate toEntity(ClientCompany company) {
        return CompanyTemplate.builder()
                .clientCompany(company)
                .name(name)
                .description(description)
                .department(department)
                .category(category)
                .yearsOfExperience(yearsOfExperience)
                .build();
    }
}
