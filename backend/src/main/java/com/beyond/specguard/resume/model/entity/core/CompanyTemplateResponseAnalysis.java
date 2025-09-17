package com.beyond.specguard.resume.model.entity.core;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "company_template_response_analysis")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CompanyTemplateResponseAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(name = "response_id", nullable = false, unique = true)
    private UUID responseId;   // 단순히 UUID 저장, DB FK 제약 없음

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "response_id",
            referencedColumnName = "id",
            insertable = false, updatable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private CompanyTemplateResponse response;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "JSON")
    private String keyword;

    @Column(name = "created_at", nullable = false,
            updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    public void updateAnalysis(String summary, String keywordJson) {
        this.summary = summary;
        this.keyword = keywordJson;
    }
}
