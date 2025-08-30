package com.beyond.specguard.resume.entity.core;

import com.beyond.specguard.resume.entity.common.BaseEntity;
import com.beyond.specguard.resume.entity.common.enums.ResumeStatus;
import jakarta.persistence.*;
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

    //PK
    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    //성명
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    //지원자 연락처
    @Column(name = "phone", nullable = false, length = 50)
    private String phone;

    //이메일
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    //status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ResumeStatus status;

    //해쉬화된 패스워드
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Builder

    public Resume(String id, String name, String phone, String email, ResumeStatus status, String passwordHash) {
        this.id = (id != null) ? id : UUID.randomUUID().toString();
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.status = status;
        this.passwordHash = passwordHash;
    }
}
