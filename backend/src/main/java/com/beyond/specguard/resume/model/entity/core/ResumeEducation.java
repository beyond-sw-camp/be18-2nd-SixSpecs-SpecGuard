package com.beyond.specguard.resume.model.entity.core;

import com.beyond.specguard.resume.model.dto.request.ResumeEducationUpsertRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "resume_education"
)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ResumeEducation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID id;

    //다대일
    //resume_id는 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "resume_id",
            nullable = false,
            columnDefinition = "CHAR(36)",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @JsonIgnore
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
    @Column(name = "admission_type", nullable = false)
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

    @CreationTimestamp
    @Column(name ="created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void update(ResumeEducationUpsertRequest req) {
        if (req.schoolName() != null ) this.schoolName = req.schoolName();
        if (req.major() != null ) this.major = req.major();
        if (req.graduationStatus() != null ) this.graduationStatus = req.graduationStatus();
        if (req.degree() != null ) this.degree = req.degree();
        if (req.admissionType() != null ) this.admissionType = req.admissionType();
        if (req.gpa() != null ) this.gpa = req.gpa();
        if (req.maxGpa() != null ) this.maxGpa = req.maxGpa();
        if (req.startDate() != null ) this.startDate = req.startDate();
        if (req.endDate() != null ) this.endDate = req.endDate();
        if (req.schoolType() != null ) this.schoolType = req.schoolType();
    }

    public enum AdmissionType {
        REGULAR,
        TRANSFER
    }

    public enum Degree {
        HIGH_SCHOOL,
        ASSOCIATE,
        BACHELOR,
        MASTER,
        DOCTORATE,
        OTHER

    }

    public enum GraduationStatus {
        ENROLLED,
        GRADUATED,
        EXPECTED,
        WITHDRAWN,
        LEAVE_OF_ABSENCE
    }

    public enum SchoolType {
        HIGH,
        UNIV,
        GRAD
    }
}
