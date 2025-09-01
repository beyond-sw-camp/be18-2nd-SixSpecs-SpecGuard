package com.beyond.specguard.resume.entity.core;

import com.beyond.specguard.resume.entity.common.BaseEntity;
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
        name = "resume_education",
        indexes = @Index(name="idx_edu_resume",
                columnList="resume_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResumeEducation extends BaseEntity {

    //PK
    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    //다대일
    //resume_id는 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    //고등학교/대학교/대학원 구분
    @Enumerated(EnumType.STRING)
    @Column(name = "school_type", nullable = false, length = 20)
    private SchoolType schoolType;

    //학교명
    @Column(name = "school_name", nullable = false, length = 150)
    private String schoolName;

    //전공 -계열/학과 계열
    @Column(name = "major", nullable = false, length = 150)
    private String major;

    //학위 구분
    @Column(name = "degree", nullable = false, length = 30)
    private String degree;

    //졸업 구분
    @Column(name = "graduation_status", nullable = false, length = 20)
    private String graduationStatus;

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
    @Column(name = "end_date")
    private LocalDate endDate;


    @Builder
    public ResumeEducation(String id, Resume resume, SchoolType schoolType, String schoolName, String major, String degree, String graduationStatus, Double gpa, Double maxGpa, LocalDate startDate, LocalDate endDate) {
        this.id = (id != null) ? id : UUID.randomUUID().toString();  // UUID
        this.resume = resume;
        this.schoolType = schoolType;
        this.schoolName = schoolName;
        this.major = major;
        this.degree = degree;
        this.graduationStatus = graduationStatus;
        this.gpa = gpa;
        this.maxGpa = maxGpa;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
