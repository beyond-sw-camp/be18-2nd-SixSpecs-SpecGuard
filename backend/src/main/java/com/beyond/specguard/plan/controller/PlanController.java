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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    // 생성
    @PostMapping
    @RequestMapping("/admins/plans")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlanResponseDto> createPlan(@Valid @RequestBody PlanRequestDto request) {
        return ResponseEntity.status(201).body(planService.createPlan(request));
    }

    // 수정
    @RequestMapping("/admins/plans")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlanResponseDto> updatePlan(
            @PathVariable UUID id,
            @Valid @RequestBody PlanRequestDto request) {
        return ResponseEntity.ok(planService.updatePlan(id, request));
    }

    // 삭제
    @RequestMapping("/admins/plans")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePlan(@PathVariable UUID id) {
        planService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @RequestMapping("/api/v1/plans")
    public ResponseEntity<PlanResponseDto> getPlan(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(planService.getPlan(id));
    }

    @GetMapping
    @RequestMapping("/api/v1/plans")
    public ResponseEntity<PlanListResponseDto> getPlans(
            @PageableDefault Pageable pageable
    ) {
        return ResponseEntity.ok(planService.getAllPlans(pageable));
    }
}
