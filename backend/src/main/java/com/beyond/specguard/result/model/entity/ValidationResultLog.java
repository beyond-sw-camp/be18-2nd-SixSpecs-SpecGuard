package com.beyond.specguard.result.model.entity;

import com.beyond.specguard.resume.model.entity.core.Resume;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name="validation_result_log"
)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@ToString
public class ValidationResultLog {
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    @Column(length = 36, columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name="validation_result_id",
            nullable = false,
            columnDefinition = "CHAR(36)",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private ValidationResult validationResult;


    @Column(name = "validation_score", nullable = false)
    private Double validationScore;

    @Column(name = "validation_resume_portfolio", nullable = true)
    private Double validationResumePortfolio;

    @Column(name = "summary", columnDefinition = "TEXT", nullable = true)
    private String summary;

    @Column(name = "keyword_list", columnDefinition = "TEXT", nullable = true)
    private String keywordList;

    @Column(name="mismatch_fields", columnDefinition = "JSON", nullable = true)
    private String mismatchFields;

    @Column(name = "validated_at", nullable = false)
    private LocalDateTime validatedAt;

    @Column(name = "decision_comment", columnDefinition = "TEXT", nullable = true)
    private String decisionComment;




}
