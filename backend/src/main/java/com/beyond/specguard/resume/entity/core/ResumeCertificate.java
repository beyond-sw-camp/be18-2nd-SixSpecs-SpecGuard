package com.beyond.specguard.resume.entity.core;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(
        name = "resume_certificate",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_resume_certificate_resume",
                columnNames = "resume_id"
        )
)
@NoArgsConstructor
public class ResumeCertificate extends ResumeBasic {
    //PK
    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    //다대일
    //resume_id는 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    //자격증 명
    @Column(name = "certificate_name", nullable = false, length = 255)
    private String certificateName;


    //자격증 발급 번호
    @Column(name = "certificate_number", nullable = false, length = 255)
    private String certificateNumber;

    //발행자
    @Column(name="issuer", nullable = false, length = 255)
    private String issuer;


    //취득 시기
    @Column(name = "issued_date", nullable = false)
    private LocalDate issuedDate;

    //자격증 URL
    @Column(name = "cert_url", nullable = true)
    private String certUrl;

    @Builder
    public ResumeCertificate(String id, Resume resume, String certificateName, String certificateNumber, String issuer, LocalDate issuedDate, String certUrl) {
        this.id = id;
        this.resume = resume;
        this.certificateName = certificateName;
        this.certificateNumber = certificateNumber;
        this.issuer = issuer;
        this.issuedDate = issuedDate;
        this.certUrl = certUrl;
    }
}
