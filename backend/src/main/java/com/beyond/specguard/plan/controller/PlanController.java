package com.beyond.specguard.plan.controller;

import com.beyond.specguard.plan.model.dto.PlanListResponseDto;
import com.beyond.specguard.plan.model.dto.PlanRequestDto;
import com.beyond.specguard.plan.model.dto.PlanResponseDto;
import com.beyond.specguard.plan.service.PlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    // 생성
    @PostMapping("/admins/plans")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlanResponseDto> createPlan(@Valid @RequestBody PlanRequestDto request) {
        return ResponseEntity.status(201).body(planService.createPlan(request));
    }

    // 수정
    @PutMapping("/admins/plans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlanResponseDto> updatePlan(
            @PathVariable UUID id,
            @Valid @RequestBody PlanRequestDto request) {
        return ResponseEntity.ok(planService.updatePlan(id, request));
    }

    // 삭제
    @DeleteMapping("/admins/plans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePlan(@PathVariable UUID id) {
        planService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/plans/{id}")
    public ResponseEntity<PlanResponseDto> getPlan(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(planService.getPlan(id));
    }

    @GetMapping("/api/v1/plans")
    public ResponseEntity<PlanListResponseDto> getPlans(
            @PageableDefault Pageable pageable
    ) {
        return ResponseEntity.ok(planService.getAllPlans(pageable));
    }
}
