package com.beyond.specguard.resume.entity.core;

import com.beyond.specguard.resume.entity.common.BaseEntity;
import com.beyond.specguard.resume.entity.common.enums.EmploymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(
        name = "resume_experience"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResumeExperience extends BaseEntity {


    //다대일
    //resume_id는 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false, columnDefinition = "CHAR(36)")
    private Resume resume;

    //회사명
    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    //부서명
    @Column(name="department", nullable = false, length = 255)
    private String department;

    //직급명
    @Column(name="position", nullable = false, length = 255)
    private String position;

    //담당 업무
    @Column(name="responsibilities", nullable = true, length = 255)
    private String responsibilities;

    //입사 시기
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    //퇴사 시기
    @Column(name = "end_date")
    private LocalDate endDate;

    //고용 형태
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false)
    private EmploymentStatus employmentStatus;


    @Builder
    public ResumeExperience( Resume resume, String companyName, String department, String position, String responsibilities, LocalDate startDate, LocalDate endDate, EmploymentStatus employmentStatus) {
        this.resume = resume;
        this.companyName = companyName;
        this.department = department;
        this.position = position;
        this.responsibilities = responsibilities;
        this.startDate = startDate;
        this.endDate = endDate;
        this.employmentStatus = employmentStatus;
    }

}
