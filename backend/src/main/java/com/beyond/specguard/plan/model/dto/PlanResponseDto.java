package com.beyond.specguard.plan.model.dto;

import com.beyond.specguard.plan.model.entity.Plan;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PlanResponseDto {
    private UUID id;
    private String name;
    private Integer price;
    private Plan.Currency currency;
    private Plan.BillingCycle billingCycle;
    private Integer resumeLimit;
    private Integer analysisLimit;
    private Integer userLimit;
    private LocalDateTime createdAt;

    public static PlanResponseDto fromEntity(Plan plan) {
        return PlanResponseDto.builder()
                .id(plan.getId())
                .name(plan.getName())
                .price(plan.getPrice())
                .currency(plan.getCurrency())
                .billingCycle(plan.getBillingCycle())
                .resumeLimit(plan.getResumeLimit())
                .analysisLimit(plan.getAnalysisLimit())
                .userLimit(plan.getUserLimit())
                .createdAt(plan.getCreatedAt())
                .build();
    }
}