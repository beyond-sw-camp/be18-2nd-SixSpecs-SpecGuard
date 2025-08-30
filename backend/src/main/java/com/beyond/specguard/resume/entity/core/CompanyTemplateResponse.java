package com.beyond.specguard.resume.entity.core;

import com.beyond.specguard.resume.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "company_template_response",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ctresp_resume_field",
                columnNames = {"resume_id", "field_id"}
        ),
        indexes = {
                @Index(name = "idx_ctresp_resume",
                        columnList = "resume_id"),
                @Index(name = "idx_ctresp_field",
                        columnList = "field_id")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyTemplateResponse extends BaseEntity {
    //PK
    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    //다대일
    //resume_id는 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "field_id", length = 36, nullable = false)
    private String fieldId;

    //지원자의 답변
    @Lob
    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @Builder

    public CompanyTemplateResponse(String id, Resume resume, String fieldId, String answer) {
        this.id = id != null ? id : java.util.UUID.randomUUID().toString();
        this.resume = resume;
        this.fieldId = fieldId;
        this.answer = answer;
    }
}
