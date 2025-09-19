package com.beyond.specguard.validation.controller;

import com.beyond.specguard.company.common.model.entity.ClientUser;
import com.beyond.specguard.company.common.model.service.CustomUserDetails;
import com.beyond.specguard.validation.model.dto.request.ValidationLogCommentRequestDto;
import com.beyond.specguard.validation.model.dto.request.ValidationCalculateRequestDto;
import com.beyond.specguard.validation.model.dto.request.ValidationPercentileRequestDto;
import com.beyond.specguard.validation.model.dto.response.ValidationResultLogResponseDto;
import com.beyond.specguard.validation.model.service.ValidationResultLogService;
import com.beyond.specguard.validation.model.service.ValidationResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/validation")
@RequiredArgsConstructor
@Tag(name = "ValidationTemplate", description = "정합성 검사 관련 API")
public class ValidationController {

    private final ValidationResultService validationResultService;
    private final ValidationResultLogService validationResultLogService;


    @Operation(
            summary = "정합성 결과 계산 API",
            description = "특정 이력서에 대해 정합성 검사를 요청하고 정합성 검사를 실행한다."
    )
    @PreAuthorize("hasAnyRole('OWNER','MANAGER','VIEWER')")
    @PostMapping("/calculate")
    public ResponseEntity<UUID> calculate(
            @Valid @RequestBody ValidationCalculateRequestDto request,
            Authentication authentication
    ) {
        ClientUser clientUser = getClientUser(authentication);
        UUID resultId = validationResultService.calculateAndSave(clientUser, request);
        return ResponseEntity.ok(resultId);
    }



    @Operation(
            summary = "정합성 점수 조회 API",
            description = "기업 사용자가 특정 이력서의 정합성 분석 결과를 조회합니다."
    )
    @PreAuthorize("hasAnyRole('OWNER','MANAGER','VIEWER')")
    @GetMapping("/{resumeId}/result")
    public ResponseEntity<List<ValidationResultLogResponseDto>> getValidationLog(
            @PathVariable UUID resumeId,
            Authentication authentication
    ) {
        ClientUser clientUser = getClientUser(authentication);
        return ResponseEntity.ok(validationResultLogService.getLogsByResumeId(clientUser, resumeId));
    }


    @Operation(
            summary = "정합성 로그 코멘트 수정 API",
            description = "특정 정합성 로그의 Comment를 작성합니다."
    )
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    @PatchMapping("/logs/{logId}/comment")
    public ResponseEntity<Void> updateComment(
            @PathVariable UUID logId,
            @Valid @RequestBody ValidationLogCommentRequestDto body,
            Authentication authentication
    ) {
        ClientUser clientUser = getClientUser(authentication);
        validationResultLogService.updateComment(clientUser, logId, body.getComment());
        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "정합성 최종 점수 추출 API",
            description = "adjusted_score에 percentile을 적용 후 최종 점수를 반환합니다."
    )
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    @PostMapping("/percentile")
    public ResponseEntity<Void> updatePercentile(
            @Valid @RequestBody ValidationPercentileRequestDto request,
            Authentication authentication
    ){
        ClientUser clientUser = getClientUser(authentication);
        validationResultService.calculatePercentile(clientUser, request);
        return ResponseEntity.noContent().build();
    }






    private ClientUser getClientUser(Authentication authentication) {
        return ((CustomUserDetails) authentication.getPrincipal()).getUser();
    }

}
