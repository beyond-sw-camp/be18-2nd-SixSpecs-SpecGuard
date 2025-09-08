package com.beyond.specguard.evaluationprofile.model.dto.response;

import com.beyond.specguard.evaluationprofile.model.entity.EvaluationProfile;
import com.beyond.specguard.evaluationprofile.model.entity.EvaluationWeight;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EvaluationProfileResponseDto {
    private UUID id;
    private String name;
    private String description;
    private Boolean isActive;
    private List<EvaluationWeightResponseDto> weights;


    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class EvaluationWeightResponseDto {
        private UUID id;
        private EvaluationWeight.WeightType weightType;
        private Float weightValue;

        public EvaluationWeightResponseDto(EvaluationWeight evaluationWeight) {
            this.id = evaluationWeight.getId();
            this.weightType = evaluationWeight.getWeightType();
            this.weightValue = evaluationWeight.getWeightValue();
        }
    }

    public static EvaluationProfileResponseDto fromEntity(EvaluationProfile evaluationProfile) {
        return EvaluationProfileResponseDto.builder()
                .id(evaluationProfile.getId())
                .name(evaluationProfile.getName())
                .description(evaluationProfile.getDescription())
                .isActive(evaluationProfile.getIsActive())
                .weights(evaluationProfile.getWeights().stream().map(EvaluationWeightResponseDto::new).toList())
                .build();
    }
}
