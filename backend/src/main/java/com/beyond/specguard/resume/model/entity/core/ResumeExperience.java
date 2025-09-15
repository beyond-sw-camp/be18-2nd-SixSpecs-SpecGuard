package com.beyond.specguard.resume.model.entity.core;

import com.beyond.specguard.resume.model.dto.request.ResumeExperienceUpsertRequest;
import com.beyond.specguard.resume.model.entity.common.enums.EmploymentStatus;
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
        name = "resume_experience"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ResumeExperience {

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

    @CreationTimestamp
    @Column(name ="created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void update(ResumeExperienceUpsertRequest req) {
        if (req.companyName() != null ) this.companyName = req.companyName();
        if (req.position() != null ) this.position = req.position();
        if (req.department() != null ) this.department = req.department();
        if (req.employmentStatus() != null)  this.employmentStatus = req.employmentStatus();
        if (req.startDate() != null) this.startDate = req.startDate();
        if (req.endDate() != null) this.endDate = req.endDate();
        if (req.responsibilities() != null) this.responsibilities = req.responsibilities();
    }
}
