package com.beyond.specguard.resume.entity.core;

import com.beyond.specguard.resume.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "company_form_submission",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_submission_company_resume",
                columnNames = {"company_id", "resume_id"}
        ),
        indexes = {
                @Index(name = "idx_submission_company", columnList = "company_id"),
                @Index(name = "idx_submission_resume", columnList = "resume_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyFormSubmission extends BaseEntity {

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "company_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false, columnDefinition = "CHAR(36)",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Resume resume;

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    @Builder
    public CompanyFormSubmission(UUID companyId, Resume resume) {
        this.companyId = companyId;
        this.resume = resume;
    }
}
