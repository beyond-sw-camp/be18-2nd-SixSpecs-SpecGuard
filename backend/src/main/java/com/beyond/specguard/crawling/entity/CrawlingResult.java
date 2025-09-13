package com.beyond.specguard.crawling.entity;

import com.beyond.specguard.resume.entity.core.ResumeLink;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name="crawling_result",
        uniqueConstraints = @UniqueConstraint(name="uk_crawl_resume_link", columnNames = {"resume_link_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CrawlingResult {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="resume_link_id", nullable = false, columnDefinition = "CHAR(36)", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ResumeLink resumeLink;

    @Enumerated(EnumType.STRING)
    @Column(name="crawling_status", nullable = false)
    private CrawlingStatus crawlingStatus;

    @Lob
    @Column(name="contents", columnDefinition = "LONGBLOB", nullable = true)
    private byte[] contents;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if(crawlingStatus == null)
            crawlingStatus = CrawlingStatus.PENDING;
    }


    public enum CrawlingStatus {
        PENDING,
        RUNNING,
        FAILED,
        COMPLETED,
        NONEXISTED,
        ANALYSIS
    }
}
