package com.beyond.specguard.resume.model.entity.core;

import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import com.beyond.specguard.resume.model.entity.common.BaseEntity;
import com.beyond.specguard.resume.model.entity.common.enums.ResumeStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@Table(name = "resume",
        indexes = {
                @Index(name = "idx_resume_template", columnList = "template_id")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class Resume extends BaseEntity {

    //template_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "template_id",
            columnDefinition = "CHAR(36)",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private CompanyTemplate template;

    //status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ResumeStatus status = ResumeStatus.DRAFT;

    //성명
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    //지원자 연락처
    @Column(name = "phone", nullable = false, length = 50)
    private String phone;

    //이메일
    @Email
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    //해쉬화된 패스워드
    @Column(name = "password_hash", columnDefinition = "CHAR(64)", nullable = false)
    private String passwordHash;

    @OneToOne(
            mappedBy = "resume",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private ResumeBasic resumeBasic;

    @OneToMany(
            mappedBy = "resume",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<ResumeCertificate> resumeCertificates = new ArrayList<>();

    @OneToMany(
            mappedBy = "resume",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<ResumeEducation> resumeEducations = new ArrayList<>();

    @OneToMany(
            mappedBy = "resume",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<ResumeExperience> resumeExperiences = new ArrayList<>();

    @OneToMany(
            mappedBy = "resume",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<ResumeLink> resumeLinks = new ArrayList<>();


    public void encodePassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void changeStatus(ResumeStatus status) { this.status = status; }

    public enum Role {
        APPLICANT;
    }
}
