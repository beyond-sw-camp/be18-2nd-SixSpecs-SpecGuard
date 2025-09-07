package com.beyond.specguard.resume.dto.response;

import com.beyond.specguard.resume.entity.common.enums.EmploymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ResumeExperienceResponse(
        UUID id,

        String companyName,

        String department,

        String position,

        String responsibilities,

        String employmentStatus,

        @Schema(type = "string", format = "date")
        LocalDate startDate,

        @Schema(type = "string", format = "date")
        LocalDate endDate
) {
}
