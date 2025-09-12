package com.beyond.specguard.resume;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@Table(name = "resume")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    //template_id
    @Column(name = "template_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID templateId;

    //status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ResumeStatus status = ResumeStatus.DRAFT;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "phone", nullable = false, length = 50)
    private String phone;

    @Email
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", columnDefinition = "CHAR(64)", nullable = false)
    private String passwordHash;

    public void changeStatus(ResumeStatus status) { this.status = status; }

    public enum ResumeStatus {
        DRAFT,
        PENDING,
        PROCESSING,
        COMPLETED,
        REVIEWED,
        REJECTED,
        ACCEPTED,
        WITHDRAWN,
        FAILED
    }

    public enum ResumeRole {
        APPLICANT
    }
}