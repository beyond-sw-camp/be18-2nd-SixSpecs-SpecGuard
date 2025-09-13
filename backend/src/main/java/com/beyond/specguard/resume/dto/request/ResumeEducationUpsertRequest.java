package com.beyond.specguard.resume.dto.request;

import com.beyond.specguard.resume.entity.common.enums.AdmissionType;
import com.beyond.specguard.resume.entity.common.enums.Degree;
import com.beyond.specguard.resume.entity.common.enums.GraduationStatus;
import com.beyond.specguard.resume.entity.common.enums.SchoolType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

public record ResumeEducationUpsertRequest(
        @Schema(description = "학력 항목 ID (없으면 생성)", example = "null 또는 UUID 문자열")
        UUID id,

        @Schema(description = "학교 구분", example = "UNIV")
        @NotNull
        SchoolType schoolType,

        @Schema(description = "학교명", example = "서울대학교")
        @NotBlank
        String schoolName,

        @Schema(description = "전공", example = "컴퓨터공학과")
        @NotBlank
        String major,

        @Schema(description = "학위", example = "BACHELOR")
        @NotNull
        Degree degree,

        @Schema(description = "졸업 구분", example = "GRADUATED")
        @NotNull
        GraduationStatus graduationStatus,

        @Schema(description = "입학 유형", example = "REGULAR")
        @NotNull
        AdmissionType admissionType,

        @Schema(description = "평점", example = "3.8")
        @NotNull
        Double gpa,

        @Schema(description = "최대 평점", example = "4.5")
        @NotNull
        Double maxGpa,

        @Schema(description = "입학일", type = "string", format = "date", example = "2015-03-01")
        @NotNull
        LocalDate startDate,

        @Schema(description = "졸업/종료일", type = "string", format = "date", example = "2019-02-28")
        LocalDate endDate
) {
}
