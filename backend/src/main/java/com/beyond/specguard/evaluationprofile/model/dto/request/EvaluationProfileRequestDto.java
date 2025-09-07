package com.beyond.specguard.evaluationprofile.model.dto.request;

import com.beyond.specguard.auth.model.entity.ClientCompany;
import com.beyond.specguard.evaluationprofile.model.entity.EvaluationProfile;
import com.beyond.specguard.evaluationprofile.model.entity.EvaluationWeight;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class EvaluationProfileRequestDto {
    @NotBlank
    private String name;

    private String description;

    @NotEmpty
    private List<WeightCreateDto> weights;

    @Getter
    @NoArgsConstructor
    public static class WeightCreateDto {
        @NotNull
        private EvaluationWeight.WeightType weightType;

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("1.0")
        private Float weightValue;

        public EvaluationWeight toEntity(EvaluationProfile profile) {
            return EvaluationWeight.builder()
                    .profile(profile)
                    .weightType(weightType)
                    .weightValue(weightValue)
                    .build();
        }
    }

    public EvaluationProfile toEntity(ClientCompany company) {
        return EvaluationProfile.builder()
                .company(company)
                .name(name)
                .description(description)
                .build();
    }
}
