package com.beyond.specguard.plan.service;

import com.beyond.specguard.plan.model.dto.PlanListResponseDto;
import com.beyond.specguard.plan.model.dto.PlanRequestDto;
import com.beyond.specguard.plan.model.dto.PlanResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface PlanService {
    @Transactional
    PlanResponseDto createPlan(PlanRequestDto request);

    @Transactional
    PlanResponseDto updatePlan(UUID id, PlanRequestDto request);

    @Transactional
    void deletePlan(UUID id);

    @Transactional(readOnly = true)
    PlanResponseDto getPlan(UUID id);

    @Transactional(readOnly = true)
    PlanListResponseDto getAllPlans(Pageable pageable);
}
