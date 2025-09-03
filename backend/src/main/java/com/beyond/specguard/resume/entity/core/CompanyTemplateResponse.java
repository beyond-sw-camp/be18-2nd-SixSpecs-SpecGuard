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
        )
        )
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyTemplateResponse extends BaseEntity {

    //다대일
    //resume_id는 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false, columnDefinition = "BINARY(16)")
    private Resume resume;

    //field_id
    @Column(name = "field_id", nullable = false, columnDefinition = "BINARY(16)")
    private String fieldId;

    //지원자의 답변
    @Lob
    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    void linkResume(Resume resume) {
        this.resume = resume;
    }

    @Builder
    public CompanyTemplateResponse( Resume resume, String fieldId, String answer) {
        this.resume = resume;
        if(resume != null){
            resume.addCompanyTemplateResponse(this);
        }
        this.fieldId = fieldId;
        this.answer = answer;
    }
}
