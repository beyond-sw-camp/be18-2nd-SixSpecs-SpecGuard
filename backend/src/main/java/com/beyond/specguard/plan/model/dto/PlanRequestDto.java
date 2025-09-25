package com.beyond.specguard.plan.model.dto;

import com.beyond.specguard.plan.model.entity.Plan;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanRequestDto {
    @NotBlank(message = "요금제 이름은 필수입니다.")
    private String name;

    @NotNull(message = "요금은 필수입니다.")
    @PositiveOrZero(message = "요금은 0 이상이어야 합니다.")
    private Integer price;

    @NotNull(message = "통화 단위는 필수입니다.")
    private Plan.Currency currency; // Enum

    @NotNull(message = "청구 주기는 필수입니다.")
    private Plan.BillingCycle billingCycle; // Enum

    @NotNull(message = "이력서 제출 제한은 필수입니다.")
    private Integer resumeLimit;

    @NotNull(message = "분석 제한은 필수입니다.")
    private Integer analysisLimit;

    @NotNull(message = "사용자 제한은 필수입니다.")
    private Integer userLimit;

    public Plan toEntity() {
        return Plan.builder()
                .name(getName())
                .price(getPrice())
                .currency(getCurrency())
                .billingCycle(getBillingCycle())
                .resumeLimit(getResumeLimit())
                .analysisLimit(getAnalysisLimit())
                .userLimit(getUserLimit())
                .build();
    }
}
