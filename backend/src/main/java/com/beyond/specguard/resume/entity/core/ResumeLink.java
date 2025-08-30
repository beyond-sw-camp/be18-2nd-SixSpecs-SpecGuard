package com.beyond.specguard.resume.entity.core;

import com.beyond.specguard.resume.entity.common.BaseEntity;
import com.beyond.specguard.resume.entity.common.enums.LinkType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "resume_link",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_resume_link_resume",
                columnNames = "resume_id"
        )
)
@NoArgsConstructor
public class ResumeLink extends BaseEntity {
    //PK
    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    //다대일
    //resume_id는 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    //url 링크
    @Column(name="url")
    private String url;

    //url 종류
    @Column(name = "link_type", nullable = false)
    private LinkType linkType;

    //원문?
    @Column(name = "contents")
    private String contents;

    @Builder

    public ResumeLink(String id, Resume resume, String url, LinkType linkType, String contents) {
        this.id = id;
        this.resume = resume;
        this.url = url;
        this.linkType = linkType;
        this.contents = contents;
    }
}
