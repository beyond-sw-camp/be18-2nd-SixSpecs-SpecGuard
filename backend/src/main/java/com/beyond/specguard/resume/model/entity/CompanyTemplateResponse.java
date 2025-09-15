package com.beyond.specguard.resume.model.entity;

import com.beyond.specguard.companytemplate.model.entity.CompanyTemplateField;
import com.beyond.specguard.resume.model.dto.request.CompanyTemplateResponseDraftUpsertRequest;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "company_template_response",
        uniqueConstraints = @UniqueConstraint(name = "uk_ctresp_resume_field",
                columnNames = {"resume_id", "field_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CompanyTemplateResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID id;

    //다대일
    //resume_id는 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "resume_id",
            nullable = false,
            columnDefinition = "CHAR(36)",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private Resume resume;

    //field_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "field_id",
            nullable = false,
            columnDefinition = "CHAR(36)",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private CompanyTemplateField companyTemplateField;

    //지원자의 답변
    @Lob
    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @CreationTimestamp
    @Column(name ="created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 엔티티 클래스 안에 추가
    public void changeAnswer(String answer) {
        this.answer = answer;
    }

    @Builder
    public CompanyTemplateResponse(Resume resume, CompanyTemplateField companyTemplateField, String answer) {
        this.resume = resume;
        this.companyTemplateField = companyTemplateField;
        this.answer = answer;
    }

    public void update(CompanyTemplateResponseDraftUpsertRequest.Item req) {
        if (req.answer() != null) this.answer = req.answer();

    }
}
