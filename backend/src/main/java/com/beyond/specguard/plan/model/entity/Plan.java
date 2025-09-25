package com.beyond.specguard.plan.model.entity;

import com.beyond.specguard.plan.model.dto.PlanRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "plan")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BillingCycle billingCycle;

    @Column(nullable = false)
    private Integer resumeLimit;

    @Column(nullable = false)
    private Integer analysisLimit;

    @Column(nullable = false)
    private Integer userLimit;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void update(PlanRequestDto request) {
        if (request.getName() != null) {
            this.name = request.getName();
        }
        if (request.getPrice() != null) {
            this.price = request.getPrice();
        }
        if (request.getCurrency() != null) {
            this.currency = request.getCurrency();
        }
        if (request.getBillingCycle() != null) {
            this.billingCycle = request.getBillingCycle();
        }
        if (request.getResumeLimit() != null) {
            this.resumeLimit = request.getResumeLimit();
        }
        if (request.getAnalysisLimit() != null) {
            this.analysisLimit = request.getAnalysisLimit();
        }
        if (request.getUserLimit() != null) {
            this.userLimit = request.getUserLimit();
        }
    }

    public enum Currency {
        KRW, USD, EUR
    }

    public enum BillingCycle {
        MONTH, YEAR
    }

}
