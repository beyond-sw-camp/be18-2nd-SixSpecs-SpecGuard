package com.beyond.specguard.resume.entity.core;

import com.beyond.specguard.resume.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "company_template_response",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_company_template_response_resume",
                columnNames = "resume_id"
        )
)
@NoArgsConstructor
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


    //지원자의 답변
    @Column(name = "answer")
    private String answer;

    @Builder

    public CompanyTemplateResponse(String id, Resume resume, String answer) {
        this.id = id;
        this.resume = resume;
        this.answer = answer;
    }
}
