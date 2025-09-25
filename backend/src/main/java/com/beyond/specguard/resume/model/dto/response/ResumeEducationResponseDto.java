package com.beyond.specguard.resume.model.dto.response;

import com.beyond.specguard.resume.model.entity.ResumeEducation;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ResumeEducationResponseDto(
        UUID id,
        String schoolName,
        String major,
        ResumeEducation.GraduationStatus graduationStatus,
        ResumeEducation.Degree degree,
        ResumeEducation.AdmissionType admissionType,
        Double gpa,
        Double maxGpa,
        LocalDate startDate,
        LocalDate endDate,
        ResumeEducation.SchoolType schoolType,
        String district,
        String city,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ResumeEducationResponseDto fromEntity(ResumeEducation resumeEducation) {
        return ResumeEducationResponseDto.builder()
                .id(resumeEducation.getId())
                .schoolName(resumeEducation.getSchoolName())
                .major(resumeEducation.getMajor())
                .graduationStatus(resumeEducation.getGraduationStatus())
                .degree(resumeEducation.getDegree())
                .admissionType(resumeEducation.getAdmissionType())
                .gpa(resumeEducation.getGpa())
                .maxGpa(resumeEducation.getMaxGpa())
                .schoolType(resumeEducation.getSchoolType())
                .endDate(resumeEducation.getEndDate())
                .startDate(resumeEducation.getStartDate())
                .district(resumeEducation.getDistrict())
                .city(resumeEducation.getCity())
                .createdAt(resumeEducation.getCreatedAt())
                .updatedAt(resumeEducation.getUpdatedAt())
                .build();
    }
}
