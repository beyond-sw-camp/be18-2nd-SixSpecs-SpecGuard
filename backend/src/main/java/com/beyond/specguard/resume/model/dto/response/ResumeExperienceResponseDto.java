package com.beyond.specguard.resume.model.dto.response;

import com.beyond.specguard.resume.model.entity.ResumeExperience;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResumeExperienceResponseDto {

    private UUID id;

    private String companyName;

    private String department;

    private String position;

    private String responsibilities;

    private LocalDate startDate;

    private LocalDate endDate;

    private ResumeExperience.EmploymentStatus employmentStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static ResumeExperienceResponseDto fromEntity(ResumeExperience e) {
        return ResumeExperienceResponseDto.builder()
                .id(e.getId())
                .companyName(e.getCompanyName())
                .department(e.getDepartment())
                .position(e.getPosition())
                .responsibilities(e.getResponsibilities())
                .startDate(e.getStartDate())
                .endDate(e.getEndDate())
                .employmentStatus(e.getEmploymentStatus())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
