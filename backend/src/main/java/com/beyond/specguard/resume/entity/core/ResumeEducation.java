package com.beyond.specguard.resume.entity.core;

import com.beyond.specguard.resume.entity.common.BaseEntity;
import com.beyond.specguard.resume.entity.common.enums.AdmissionType;
import com.beyond.specguard.resume.entity.common.enums.Degree;
import com.beyond.specguard.resume.entity.common.enums.GraduationStatus;
import com.beyond.specguard.resume.entity.common.enums.SchoolType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "resume_education"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResumeEducation extends BaseEntity {


    //다대일
    //resume_id는 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false,  columnDefinition = "BINARY(16)")
    private Resume resume;

    //학교명
    @Column(name = "school_name", nullable = false, length = 255)
    private String schoolName;

    //전공 -계열/학과 계열
    @Column(name = "major", nullable = true, length = 255)
    private String major;

    //졸업 구분
    @Enumerated(EnumType.STRING)
    @Column(name = "graduation_status", nullable = false)
    private GraduationStatus graduationStatus;

    //학위 구분
    @Enumerated(EnumType.STRING)
    @Column(name = "degree", nullable = false, length = 100)
    private Degree degree;

    //입학, 편입
    @Enumerated(EnumType.STRING)
    @Column(name="admission_type", nullable = false)
    private AdmissionType admissionType;

    //학점
    @Column(name = "gpa", nullable = false)
    private Double gpa;

    //최대 학점
    @Column(name = "max_gpa", nullable = false)
    private Double maxGpa;

    //입학일
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    //졸업일
    @Column(name = "end_date", nullable = true)
    private LocalDate endDate;


    //고등학교/대학교/대학원 구분
    @Enumerated(EnumType.STRING)
    @Column(name = "school_type", nullable = false, length = 20)
    private SchoolType schoolType;


    void linkResume(Resume resume) {
        this.resume = resume;
    }




    @Builder
    public ResumeEducation( Resume resume, SchoolType schoolType, String schoolName, String major, Degree degree, GraduationStatus graduationStatus, AdmissionType admissionType, Double gpa, Double maxGpa, LocalDate startDate, LocalDate endDate) {
        this.resume = resume;
        if(resume != null) {
            resume.addEducation(this);
        }
        this.schoolType = schoolType;
        this.schoolName = schoolName;
        this.major = major;
        this.degree = degree;
        this.graduationStatus = graduationStatus;
        this.admissionType = admissionType;
        this.gpa = gpa;
        this.maxGpa = maxGpa;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
