package com.beyond.specguard.resume.entity.core;

import com.beyond.specguard.resume.entity.common.BaseEntity;
import com.beyond.specguard.resume.entity.common.enums.ResumeStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Entity
@Table(name = "resume")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Resume extends BaseEntity {


    //template_id
    @Column(name = "template_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID templateId;

    //status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ResumeStatus status;

    //default 값
    @PrePersist
    void prePersist() {
        if(status == null) status = ResumeStatus.DRAFT;
    }

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

    //ResumeBasic이랑 OneToOne
    @OneToOne(mappedBy = "resume", fetch = FetchType.LAZY,
    cascade = CascadeType.ALL, orphanRemoval = true)
    private ResumeBasic basic;

    void linkBasic(ResumeBasic basic){
        this.basic = basic;
    }

    //ResumeEducation
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeEducation> educations = new ArrayList<>();

    public void addEducation(ResumeEducation education) {
        this.educations.add(education);
        if (education.getResume() != this) {
            education.linkResume(this);
        }
    }

    //ResumeExperience
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeExperience> experiences = new ArrayList<>();

    public void addExperience(ResumeExperience experience) {
        this.experiences.add(experience);
        if(experience.getResume() != this){
            experience.linkResume(this);
        }
    }

    //ResumeCertificate
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeCertificate> certificates = new ArrayList<>();
    public void addCertificate(ResumeCertificate certificate) {
        this.certificates.add(certificate);
        if(certificate.getResume() != this){
            certificate.linkResume(this);
        }
    }

    //ResumeLink
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeLink> links = new ArrayList<>();
    public void addLink(ResumeLink link) {
        this.links.add(link);
        if(link.getResume() != this){
            link.linkResume(this);
        }
    }

    //CompanyTemplateResponse
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompanyTemplateResponse> companyTemplateResponses = new ArrayList<>();
    public void addCompanyTemplateResponse(CompanyTemplateResponse companyTemplateResponse) {
        this.companyTemplateResponses.add(companyTemplateResponse);
        if(companyTemplateResponse.getResume() != this){
            companyTemplateResponse.linkResume(this);
        }
    }


    @Builder
    public Resume(UUID templateId, ResumeStatus status, String name, String phone, String email, String passwordHash) {
        this.templateId = templateId;
        this.status = status;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.passwordHash = passwordHash;
    }


    // 세터 없이 변경용
    public void changeStatus(ResumeStatus status) { this.status = status; }
    public void changeName(String name) { this.name = name; }
    public void changePhone(String phone) { this.phone = phone; }
    public void changeEmail(String email) { this.email = email; }
    public void changeTemplateId(UUID templateId) { this.templateId = templateId; }
    public void changePasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

}
