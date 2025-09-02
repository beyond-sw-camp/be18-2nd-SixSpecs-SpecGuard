package com.beyond.specguard.resume.dto.education;

import com.beyond.specguard.resume.entity.common.enums.AdmissionType;
import com.beyond.specguard.resume.entity.common.enums.Degree;
import com.beyond.specguard.resume.entity.common.enums.GraduationStatus;
import com.beyond.specguard.resume.entity.common.enums.SchoolType;

import java.time.LocalDate;

public record ResumeEducationCreateRequest(
        String resumeId,
        SchoolType schoolType,
        String schoolName,
        String major,
        Degree degree,
        AdmissionType admissionType,
        GraduationStatus graduationStatus,
        Double gpa,
        Double maxGpa,
        LocalDate startDate,
        LocalDate endDate
) {
}
