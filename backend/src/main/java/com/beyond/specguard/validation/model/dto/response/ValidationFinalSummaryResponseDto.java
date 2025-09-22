package com.beyond.specguard.validation.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationFinalSummaryResponseDto {
    private UUID resultId;
    private UUID resumeId;
    private Double finalScore;         // 0~100
    private String matchKeyword;       // comma-separated
    private String mismatchKeyword;    // comma-separated
    private LocalDateTime resultAt;    // 최종 점수 계산 시각
    private String descriptionComment; // 최종 결과 코멘트
}