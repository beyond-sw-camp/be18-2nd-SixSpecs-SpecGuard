package com.beyond.specguard.crawling.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "portfolio_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PortfolioResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    //  CrawlingResult 와 1:1 매핑
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crawling_result_id", nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private CrawlingResult crawlingResult;


    @Column(name = "processed_contents", columnDefinition = "JSON")
    private String processedContents;


    @Enumerated(EnumType.STRING)
    @Column(name = "portfolio_status", nullable = false, length = 20)
    private PortfolioStatus portfolioStatus;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 상태 업데이트 유틸 메서드
    public void updateStatus(PortfolioStatus status) {
        this.portfolioStatus = status;
    }

    public enum PortfolioStatus {
        PENDING,     // 대기
        RUNNING,     // 진행 중
        COMPLETED,   // 완료
        FAILED,      // 실패
        NOTEXISTED   // (선택) 없는 경우 표시
    }
}
