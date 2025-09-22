package com.beyond.specguard.evaluationprofile.model.dto.request;

import com.beyond.specguard.company.common.model.entity.ClientCompany;
import com.beyond.specguard.evaluationprofile.model.entity.EvaluationProfile;
import com.beyond.specguard.evaluationprofile.model.entity.EvaluationWeight;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class EvaluationProfileRequestDto {
    @NotBlank(message = "프로필 이름은 필수 입력값입니다.")
    @Size(max = 50, message = "프로필 이름은 최대 255자까지 입력 가능합니다.")
    private String name;


    @Schema(description = "설명", example = "string")
    private String description;


    @Schema(description = "템플릿 ID(UUID)", example = "6d6bb9f2-f696-418c-a8db-84e859bca5fb")
    @NotNull(message = "companyTemplateId는 필수입니다.")
    private UUID companyTemplateId;

    @ArraySchema(
            arraySchema = @Schema(description = "가중치 목록"),
            schema = @Schema(
                    implementation = WeightCreateDto.class,
                    example = """
            {
              "weightType": "GITHUB_REPO_COUNT",
              "weightValue": 0.1
            }
            """
            )
    )
    @NotEmpty(message = "최소 1개 이상의 가중치가 필요합니다.")
    private List<WeightCreateDto> weights;

    @Getter
    @NoArgsConstructor
    public static class WeightCreateDto {
        @Schema(example = "GITHUB_REPO_COUNT")
        @NotNull(message = "가중치 타입은 필수입니다.")
        private EvaluationWeight.WeightType weightType;

        @Schema(example = "0.1")
        @NotNull(message = "가중치 값은 필수입니다.")
        @DecimalMin(value = "0.0", message = "가중치 값은 0.0 이상이어야 합니다.")
        @DecimalMax(value = "1.0", message = "가중치 값은 1.0 이하여야 합니다.")
        private Float weightValue;

        public EvaluationWeight fromEntity(EvaluationProfile profile) {
            return EvaluationWeight.builder()
                    .profile(profile)
                    .weightType(weightType)
                    .weightValue(weightValue)
                    .build();
        }
    }

    public EvaluationProfile toEntity(ClientCompany company, CompanyTemplate template ) {
        return EvaluationProfile.builder()
                .company(company)
                .companyTemplate(template)
                .name(name)
                .description(description)
                .build();
    }
}
