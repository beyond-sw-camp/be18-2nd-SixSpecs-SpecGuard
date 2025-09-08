package com.beyond.specguard.companytemplate.model.entity;

import com.beyond.specguard.companytemplate.model.dto.request.TemplateFieldRequestDto;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "company_template_field")
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class CompanyTemplateField {
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    @Column(length = 36, columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "template_id",
            nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private CompanyTemplate template;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false, length = 20)
    private FieldType fieldType;

    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private boolean isRequired = false;

    @Column(name = "field_order")
    private Integer fieldOrder;

    @Column(columnDefinition = "JSON")
    private String options;

    @Column(name = "min_length", nullable = false)
    @Builder.Default
    private Integer minLength = 0;

    @Column(name = "max_length", nullable = false)
    @Builder.Default
    private Integer maxLength = 500;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    protected void setTemplate(CompanyTemplate companyTemplate) {
        this.template = companyTemplate;
    }


    // FieldType ENUM 정의
    public enum FieldType {
        TEXT,
        NUMBER,
        DATE,
        SELECT
    }

    public void update(TemplateFieldRequestDto requestDto) {
        if (requestDto.getFieldName() != null) {
            this.fieldName = requestDto.getFieldName();
        }
        if (requestDto.getFieldType() != null) {
            this.fieldType = requestDto.getFieldType();
        }
        if (requestDto.getOptions() != null) {
            this.options = requestDto.getOptionsByString();
        }
        if (requestDto.getIsRequired() != null) {
            this.isRequired = requestDto.getIsRequired();
        }
        if (requestDto.getFieldOrder() != null) {
            this.fieldOrder = requestDto.getFieldOrder();
        }
        if (requestDto.getMinLength() != null) {
            this.minLength = requestDto.getMinLength();
        }
        if (requestDto.getMaxLength() != null) {
            this.maxLength = requestDto.getMaxLength();
        }
    }
}
