package com.beyond.specguard.resume.entity.core;

import com.beyond.specguard.resume.entity.common.BaseEntity;
import com.beyond.specguard.resume.entity.common.enums.ResumeStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@Table(name = "resume")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Resume extends BaseEntity {


    //template_id
    @Column(name = "template_id", columnDefinition = "CHAR(36)", nullable = false)
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


    @Builder
    public Resume(UUID templateId, ResumeStatus status, String name, String phone, String email, String passwordHash) {
        this.templateId = templateId;
        this.status = status;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public void changeStatus(ResumeStatus status) { this.status = status; }

}
