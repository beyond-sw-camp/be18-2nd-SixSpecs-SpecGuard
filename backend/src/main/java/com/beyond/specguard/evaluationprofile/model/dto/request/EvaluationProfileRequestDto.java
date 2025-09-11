package com.beyond.specguard.evaluationprofile.model.dto.request;

import com.beyond.specguard.auth.model.entity.ClientCompany;
import com.beyond.specguard.evaluationprofile.model.entity.EvaluationProfile;
import com.beyond.specguard.evaluationprofile.model.entity.EvaluationWeight;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class EvaluationProfileRequestDto {
    @NotBlank(message = "프로필 이름은 필수 입력값입니다.")
    @Size(max = 50, message = "프로필 이름은 최대 255자까지 입력 가능합니다.")
    private String name;


    private String description;

    @NotEmpty(message = "최소 1개 이상의 가중치가 필요합니다.")
    private List<WeightCreateDto> weights;

    @Getter
    @NoArgsConstructor
    public static class WeightCreateDto {
        @NotNull(message = "가중치 타입은 필수입니다.")
        private EvaluationWeight.WeightType weightType;

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

    public EvaluationProfile fromEntity(ClientCompany company) {
        return EvaluationProfile.builder()
                .company(company)
                .name(name)
                .description(description)
                .build();
    }
}
