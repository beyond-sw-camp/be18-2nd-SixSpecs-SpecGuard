package com.beyond.specguard.resume.model.dto.request;

import com.beyond.specguard.resume.model.entity.Resume;
import com.beyond.specguard.resume.model.entity.ResumeEducation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ResumeEducationUpsertRequest(
        @Schema(description = "학력 항목 ID (없으면 생성)", example = "null 또는 UUID 문자열")
        UUID id,

        @Schema(description = "학교 구분", example = "UNIV")
        @NotNull
        ResumeEducation.SchoolType schoolType,

        @Schema(description = "학교명", example = "서울대학교")
        @NotBlank
        String schoolName,

        @Schema(description = "전공", example = "컴퓨터공학과")
        String major,

        @Schema(description = "학위", example = "BACHELOR")
        ResumeEducation.Degree degree,

        @Schema(description = "졸업 구분", example = "GRADUATED")
        @NotNull
        ResumeEducation.GraduationStatus graduationStatus,

        @Schema(description = "입학 유형", example = "REGULAR")
        ResumeEducation.AdmissionType admissionType,

        @Schema(description = "평점", example = "3.8")
        Double gpa,

        @Schema(description = "최대 평점", example = "4.5")
        Double maxGpa,

        @Schema(description = "시/도", example = "서울시")
        @NotNull
        String city,

        @Schema(description = "구/군", example = "강남구")
        @NotNull
        String district,

        @Schema(description = "입학일", type = "string", format = "date", example = "2015-03-01")
        @NotNull
        LocalDate startDate,

        @Schema(description = "졸업/종료일", type = "string", format = "date", example = "2019-02-28")
        LocalDate endDate
) {
        public ResumeEducation toEntity(Resume resume) {
                return ResumeEducation.builder()
                        .id(id)
                        .resume(resume)
                        .admissionType(admissionType)
                        .gpa(gpa)
                        .maxGpa(maxGpa)
                        .degree(degree)
                        .graduationStatus(graduationStatus)
                        .admissionType(admissionType)
                        .startDate(startDate)
                        .endDate(endDate)
                        .schoolType(schoolType)
                        .schoolName(schoolName)
                        .city(city)
                        .district(district)
                        .major(major)
                        .build();
        }
}
