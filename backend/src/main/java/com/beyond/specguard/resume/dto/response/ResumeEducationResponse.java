package com.beyond.specguard.resume.dto.response;

import com.beyond.specguard.resume.entity.common.enums.AdmissionType;
import com.beyond.specguard.resume.entity.common.enums.Degree;
import com.beyond.specguard.resume.entity.common.enums.GraduationStatus;
import com.beyond.specguard.resume.entity.common.enums.SchoolType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ResumeEducationResponse(
        String id,
        String resumeId,
        SchoolType schoolType,
        String schoolName,
        String major,
        Degree degree,
        GraduationStatus graduationStatus,
        AdmissionType admissionType,
        Double gpa,
        Double maxGpa,
        LocalDate startDate,
        LocalDate endDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
