package com.beyond.specguard.resume.entity.core;

import com.beyond.specguard.resume.entity.common.BaseEntity;
import com.beyond.specguard.resume.entity.common.enums.LinkType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "resume_link",
        indexes = @Index(name = "idx_link_resume",
                columnList = "resume_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResumeLink extends BaseEntity {


    //다대일
    //resume_id는 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    //url 링크
    @Column(name="url")
    private String url;

    //url 종류
    @Enumerated(EnumType.STRING)
    @Column(name = "link_type", nullable = false)
    private LinkType linkType;

    //원문
    @Lob
    @Column(name = "contents", columnDefinition = "TEXT")
    private String contents;

    @Builder
    public ResumeLink( Resume resume, String url, LinkType linkType, String contents) {
        this.resume = resume;
        this.url = url;
        this.linkType = linkType;
        this.contents = contents;
    }
}
