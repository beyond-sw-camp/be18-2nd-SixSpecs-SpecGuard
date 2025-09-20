package com.beyond.specguard.validation.model.entity;


import com.beyond.specguard.resume.model.entity.Resume;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name="validation_result"
)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@ToString
public class ValidationResult {
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    @Column(length = 36, columnDefinition = "CHAR(36)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name="resume_id",
            nullable = false,
            columnDefinition = "CHAR(36)",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @JdbcTypeCode(SqlTypes.CHAR)
    private Resume resume;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name="validation_issue_id",
            nullable=false,
            columnDefinition = "CHAR(36)",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private ValidationIssue validationIssue;

    @Column(name = "adjusted_total", nullable = false)
    private Double adjustedTotal;

    @Column(name="final_score")
    private Double finalScore;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;






}
