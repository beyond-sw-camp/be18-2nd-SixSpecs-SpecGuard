package com.beyond.specguard.plan.controller;

import com.beyond.specguard.plan.model.dto.PlanListResponseDto;
import com.beyond.specguard.plan.model.dto.PlanRequestDto;
import com.beyond.specguard.plan.model.dto.PlanResponseDto;
import com.beyond.specguard.plan.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "Plans", description = "요금제 관련 API")
public class PlanController {

    private final PlanService planService;

    // 생성
    @Operation(summary = "요금제 생성", description = "관리자가 새로운 요금제를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "요금제 생성 성공",
                    content = @Content(schema = @Schema(implementation = PlanResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @PostMapping("/admins/plans")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlanResponseDto> createPlan(
            @Valid @RequestBody PlanRequestDto request) {
        return ResponseEntity.status(201).body(planService.createPlan(request));
    }

    // 수정
    @Operation(summary = "요금제 수정", description = "관리자가 기존 요금제를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요금제 수정 성공",
                    content = @Content(schema = @Schema(implementation = PlanResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "해당 요금제를 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @PutMapping("/admins/plans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlanResponseDto> updatePlan(
            @Parameter(description = "수정할 요금제 ID") @PathVariable UUID id,
            @Valid @RequestBody PlanRequestDto request) {
        return ResponseEntity.ok(planService.updatePlan(id, request));
    }

    // 삭제
    @Operation(summary = "요금제 삭제", description = "관리자가 특정 요금제를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공 (응답 본문 없음)"),
            @ApiResponse(responseCode = "404", description = "해당 요금제를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "현재 사용 중인 요금제라 삭제 불가"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @DeleteMapping("/admins/plans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePlan(
            @Parameter(description = "삭제할 요금제 ID") @PathVariable UUID id) {
        planService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }

    // 단일 조회
    @Operation(summary = "요금제 단일 조회", description = "특정 요금제의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PlanResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "해당 요금제를 찾을 수 없음")
    })
    @GetMapping("/api/v1/plans/{id}")
    public ResponseEntity<PlanResponseDto> getPlan(
            @Parameter(description = "조회할 요금제 ID") @PathVariable UUID id) {
        return ResponseEntity.ok(planService.getPlan(id));
    }

    // 전체 조회 (Pageable)
    @Operation(summary = "요금제 전체 조회", description = "모든 요금제를 페이징/정렬하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PlanListResponseDto.class)))
    })
    @GetMapping("/api/v1/plans")
    public ResponseEntity<PlanListResponseDto> getPlans(
            @ParameterObject @Parameter(description = "페이지네이션 및 정렬 정보")
            @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(planService.getAllPlans(pageable));
    }
}
