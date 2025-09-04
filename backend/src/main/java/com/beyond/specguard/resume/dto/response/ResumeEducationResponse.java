package com.beyond.specguard.resume.dto.response;

import com.beyond.specguard.resume.entity.common.enums.AdmissionType;
import com.beyond.specguard.resume.entity.common.enums.Degree;
import com.beyond.specguard.resume.entity.common.enums.GraduationStatus;
import com.beyond.specguard.resume.entity.common.enums.SchoolType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ResumeEducationResponse(
        UUID id,

        String schoolType,

        String schoolName,

        String major,

        String degree,

        String graduationStatus,

        String admissionType,

        Double gpa,

        Double maxGpa,

        @Schema(type = "string", format = "date")
        LocalDate startDate,

        @Schema(type = "string", format = "date")
        LocalDate endDate
) {
}
