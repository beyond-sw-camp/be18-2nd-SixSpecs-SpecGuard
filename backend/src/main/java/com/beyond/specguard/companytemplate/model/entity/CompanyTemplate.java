package com.beyond.specguard.companytemplate.model.entity;

import com.beyond.specguard.company.common.model.entity.ClientCompany;
import com.beyond.specguard.companytemplate.model.dto.request.CompanyTemplateBasicRequestDto;
import com.beyond.specguard.companytemplate.model.dto.request.CompanyTemplateDetailRequestDto;
import com.beyond.specguard.evaluationprofile.model.entity.EvaluationProfile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
  name = "company_template",
  indexes = {
     @Index(name = "idx_ct_end_date", columnList = "end_date")
  }
)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@ToString
public class CompanyTemplate {
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    @Column(length = 36, columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "company_id",
            nullable = false,
            columnDefinition = "CHAR(36)",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private ClientCompany clientCompany;


    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "years_of_experience", nullable = false)
    @Builder.Default
    private Integer yearsOfExperience = 0;

    @Column(name = "start_date", nullable = false)
    @Builder.Default
    // not null 제약조건에 의한 임시값
    private LocalDateTime startDate = LocalDateTime.now();

    @Column(name = "end_date", nullable = false)
    @Builder.Default
    // not null 제약조건에 의한 임시값
    private LocalDateTime endDate = LocalDateTime.now().plusDays(7);

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private TemplateStatus status = TemplateStatus.DRAFT;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(
            mappedBy = "template",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<CompanyTemplateField> fields = new ArrayList<>();


    public void update(CompanyTemplateBasicRequestDto requestDto) {
        if (requestDto.getName() != null) {
            this.name = requestDto.getName();
        }
        if (requestDto.getDepartment() != null) {
            this.department = requestDto.getDepartment();
        }
        if (requestDto.getCategory() != null) {
            this.category = requestDto.getCategory();
        }
        if (requestDto.getDescription() != null) {
            this.description = requestDto.getDescription();
        }
        if (requestDto.getYearsOfExperience() != null) {
            this.yearsOfExperience = requestDto.getYearsOfExperience();
        }
    }

    public void update(CompanyTemplateDetailRequestDto requestDto) {
        this.startDate = requestDto.getDetailDto().getStartDate();
        this.endDate = requestDto.getDetailDto().getEndDate();
    }

    public void setStatusActive() {
        this.status = TemplateStatus.ACTIVE;
    }

    public void addField(CompanyTemplateField field) {
        this.fields.add(field);
        field.setTemplate(this);
    }

    public enum TemplateStatus {
        DRAFT,        // 작성 중
        ACTIVE,       // 진행 중
        EXPIRED,      // 마감됨
        DELETED       // 삭제됨
    }
}
