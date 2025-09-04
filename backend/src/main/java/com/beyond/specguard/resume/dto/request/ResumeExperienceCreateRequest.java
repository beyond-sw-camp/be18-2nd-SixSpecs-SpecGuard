package com.beyond.specguard.resume.dto.request;

import com.beyond.specguard.resume.entity.common.enums.EmploymentStatus;

import java.time.LocalDate;

public record ResumeExperienceCreateRequest(
        String companyName,
        String department,
        String position,
        String responsibilities,
        LocalDate startDate,
        LocalDate endDate,
        EmploymentStatus employmentStatus
) {
}
