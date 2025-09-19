package com.beyond.specguard.crawling.entity;

import com.beyond.specguard.resume.model.entity.Resume;
import com.beyond.specguard.resume.model.entity.ResumeLink;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name="crawling_result",
        uniqueConstraints = @UniqueConstraint(
                name="uk_crawl_resume_link",
                columnNames = {"resume_link_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
public class CrawlingResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    // ResumeLink와 1:1
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name="resume_link_id",
            nullable = false,
            columnDefinition = "CHAR(36)",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private ResumeLink resumeLink;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false, columnDefinition = "CHAR(36)", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Resume resume;

    @Enumerated(EnumType.STRING)
    @Column(name="crawling_status", nullable = false)
    private CrawlingStatus crawlingStatus;

    @Lob
    @Column(name = "contents", columnDefinition = "LONGBLOB")
    private byte[] contents;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (crawlingStatus == null) {
            crawlingStatus = CrawlingStatus.PENDING;
        }
    }

    public enum CrawlingStatus {
        PENDING,
        RUNNING,
        FAILED,
        COMPLETED,
        NOTEXISTED
    }

    @Builder
    public CrawlingResult(Resume resume, ResumeLink resumeLink, CrawlingStatus crawlingStatus) {
        this.resume = resume;
        this.resumeLink = resumeLink;
        this.crawlingStatus = crawlingStatus != null ? crawlingStatus : CrawlingStatus.PENDING;
    }

    public void updateContents(byte[] compressed) {
        this.contents = compressed;
    }


    public void updateStatus(CrawlingStatus status) {
        this.crawlingStatus = status;
    }

}
