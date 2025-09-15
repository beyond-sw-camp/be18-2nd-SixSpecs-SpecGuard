package com.beyond.specguard.resume.model.entity.core;

import com.beyond.specguard.resume.model.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@Table(name = "company_template_response",
        uniqueConstraints = @UniqueConstraint(name = "uk_ctresp_resume_field",
                columnNames = {"resume_id", "field_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyTemplateResponse extends BaseEntity {

    //다대일
    //resume_id는 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false, columnDefinition = "CHAR(36)", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Resume resume;

    //field_id
    @Column(name = "field_id", nullable = false, columnDefinition = "CHAR(36)")
    private UUID fieldId;

    //지원자의 답변
    @Lob
    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    // 엔티티 클래스 안에 추가
    public void changeAnswer(String answer) {
        this.answer = answer;
    }

    @Builder
    public CompanyTemplateResponse(Resume resume, UUID fieldId, String answer) {
        this.resume = resume;
        this.fieldId = fieldId;
        this.answer = answer;
    }
}
