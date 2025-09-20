package com.beyond.specguard.evaluationprofile.model.entity;

import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.evaluationprofile.exception.errorcode.EvaluationProfileErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

@Entity
@Table(name = "evaluation_weight")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class EvaluationWeight {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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
        GITHUB_COMMIT_COUNT,
        GITHUB_TOPIC_MATCH,
        GITHUB_KEYWORD_MATCH,
        NOTION_KEYWORD_MATCH,
        VELOG_POST_COUNT,
        VELOG_RECENT_ACTIVITY,
        VELOG_KEYWORD_MATCH,
        CERTIFICATE_MATCH;

        @JsonCreator
        public static WeightType from(String value) {
            return Arrays.stream(values())
                    .filter(type -> type.name().equalsIgnoreCase(value))
                    .findFirst()
                    .orElseThrow(() -> new CustomException(EvaluationProfileErrorCode.INVALID_WEIGHT_TYPE));
        }
    }
}
