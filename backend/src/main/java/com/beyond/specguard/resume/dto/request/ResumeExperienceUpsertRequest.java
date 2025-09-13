package com.beyond.specguard.resume.dto.request;

import com.beyond.specguard.resume.entity.common.enums.EmploymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ResumeExperienceUpsertRequest(
        @Schema(description = "경력 항목 ID (없으면 생성)")
        UUID id,

        @Schema(description = "회사명", example = "비욘드")
        @NotBlank
        String companyName,

        @Schema(description = "부서명", example = "플랫폼개발팀")
        @NotBlank String department,

        @Schema(description = "직급/직책", example = "백엔드 개발자")
        @NotBlank
        String position,

        @Schema(description = "담당 업무", example = "주문/결제/정산 시스템 개발")
        String responsibilities,

        @Schema(description = "고용 상태", example = "EMPLOYED")
        @NotNull
        EmploymentStatus employmentStatus,

        @Schema(description = "입사일", type = "string", format = "date", example = "2021-01-01")
        @NotNull
        LocalDate startDate,

        @Schema(description = "퇴사일", type = "string", format = "date", example = "2023-06-30")
        LocalDate endDate
) {
}
