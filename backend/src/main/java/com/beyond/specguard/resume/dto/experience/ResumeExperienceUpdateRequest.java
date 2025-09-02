package com.beyond.specguard.resume.dto.experience;

import com.beyond.specguard.resume.entity.common.enums.EmploymentStatus;

import java.time.LocalDate;

public record ResumeExperienceUpdateRequest(
        String companyName,
        String department,
        String position,
        String responsibilities,
        LocalDate startDate,
        LocalDate endDate,
        EmploymentStatus employmentStatus
) {
}
