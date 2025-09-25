package com.beyond.specguard.plan.service;

import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.plan.exception.errorcode.PlanErrorCode;
import com.beyond.specguard.plan.model.dto.PlanListResponseDto;
import com.beyond.specguard.plan.model.dto.PlanRequestDto;
import com.beyond.specguard.plan.model.dto.PlanResponseDto;
import com.beyond.specguard.plan.model.entity.Plan;
import com.beyond.specguard.plan.model.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {
    private final PlanRepository planRepository;

    @Override
    @Transactional
    public PlanResponseDto createPlan(PlanRequestDto request) {
        Plan plan = request.toEntity();

        return PlanResponseDto.fromEntity(planRepository.saveAndFlush(plan));
    }

    @Override
    @Transactional
    public PlanResponseDto updatePlan(UUID id, PlanRequestDto request) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new CustomException(PlanErrorCode.PLAN_NOT_FOUND));

        plan.update(request);

        return PlanResponseDto.fromEntity(planRepository.saveAndFlush(plan));
    }

    @Override
    @Transactional
    public void deletePlan(UUID id) {
        if (!planRepository.existsById(id)) {
            throw new CustomException(PlanErrorCode.PLAN_NOT_FOUND);
        }
        planRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PlanResponseDto getPlan(UUID id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new CustomException(PlanErrorCode.PLAN_NOT_FOUND));
        return PlanResponseDto.fromEntity(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public PlanListResponseDto getAllPlans(Pageable pageable) {
        Page<PlanResponseDto> responseDtos = planRepository.findAll(pageable)
                .map(PlanResponseDto::fromEntity);

        long totalCounts = planRepository.count();

        return PlanListResponseDto.builder()
                .responseDtos(responseDtos.getContent())
                .currentPage(responseDtos.getNumber())
                .totalPages(responseDtos.getTotalPages())
                .totalElements(totalCounts)
                .size(responseDtos.getSize())
                .build();
    }
}
