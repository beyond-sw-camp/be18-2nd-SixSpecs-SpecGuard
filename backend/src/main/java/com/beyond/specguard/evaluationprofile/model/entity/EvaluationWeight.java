package com.beyond.specguard.evaluationprofile.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "evaluation_weight")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class EvaluationWeight {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "evaluation_profile_id",
            nullable = false,
            columnDefinition = "CHAR(36)",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private EvaluationProfile profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "weight_type", nullable = false)
    private WeightType weightType;

    @Column(name = "weight_value", nullable = false)
    private Float weightValue;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 상태 변경용 메서드
    public void updateWeightValue(Float weightValue) {
        this.weightValue = weightValue;
    }

    protected void setProfile(EvaluationProfile profile) {
        this.profile = profile;
    }

    public enum WeightType {
        GITHUB_REPO_COUNT,
        GITHUB_COMMIT_FREQUENCY,
        GITHUB_TOPIC_MATCH,
        GITHUB_CONSISTENCY,
        NOTION_PROJECT_COUNT,
        NOTION_DETAIL_DEPTH,
        NOTION_KEYWORD_MATCH,
        VELOG_POST_COUNT,
        VELOG_RECENT_ACTIVITY,
        VELOG_KEYWORD_MATCH,
        CAREER_MATCH,
        CERTIFICATE_MATCH,
        KEYWORD_MATCH
    }
}
