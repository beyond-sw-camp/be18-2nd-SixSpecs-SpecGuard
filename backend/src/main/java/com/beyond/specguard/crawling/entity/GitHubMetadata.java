package com.beyond.specguard.crawling.entity;


import com.beyond.specguard.resume.model.entity.ResumeLink;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "github_resume_summary",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_resume_summary_resume_link_id",   // 이름도 함께 변경
                columnNames = {"resume_link_id"}             // ✅ resume_id → resume_link_id
        )
)

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GitHubMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "resume_link_id",   // FK 컬럼명
            nullable = false,
            columnDefinition = "CHAR(36)",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private ResumeLink resumeLink;    // 관계 대상 엔티티도 Resume → ResumeLink 로 변경


//    @Column(name = "repository_count", nullable = false)
//    private int repositoryCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "language_stats", columnDefinition = "JSON")
    private Map<String, Integer> languageStats;

//    // 새 필드: 커밋 수
//    @Column(name = "commit_count", nullable = false)
//    private int commitCount;

    // 새 필드: 데이터가 들어간 시간
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void updateStats( Map<String, Integer> languageStats) {
//        this.repositoryCount = repositoryCount;
        this.languageStats = languageStats;
//        this.commitCount = commitCount;
    }
}