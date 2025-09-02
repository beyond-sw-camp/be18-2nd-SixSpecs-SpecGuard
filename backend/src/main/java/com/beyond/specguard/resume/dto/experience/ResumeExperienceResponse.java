package com.beyond.specguard.resume.dto.experience;

import com.beyond.specguard.resume.entity.common.enums.EmploymentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ResumeExperienceResponse(
        String id,
        String resumeId,
        String companyName,
        String department,
        String position,
        String responsibilities,
        LocalDate startDate,
        LocalDate endDate,
        EmploymentStatus employmentStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
