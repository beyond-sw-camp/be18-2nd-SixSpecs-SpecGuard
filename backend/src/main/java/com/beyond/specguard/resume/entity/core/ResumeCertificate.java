package com.beyond.specguard.resume.entity.core;

import com.beyond.specguard.resume.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

@Getter
@Entity
@Table(
        name = "resume_certificate"
)
@NoArgsConstructor
public class ResumeCertificate extends BaseEntity {


    //다대일
    //resume_id는 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false, columnDefinition = "BINARY(16)")
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
    @URL
    @Column(name = "cert_url", nullable = true, columnDefinition = "TEXT")
    private String certUrl;

    void linkResume(Resume resume) {
        this.resume = resume;
    }

    @Builder
    public ResumeCertificate( Resume resume, String certificateName, String certificateNumber, String issuer, LocalDate issuedDate, String certUrl) {
        this.resume = resume;
        if(resume != null){
            resume.addCertificate(this);
        }
        this.certificateName = certificateName;
        this.certificateNumber = certificateNumber;
        this.issuer = issuer;
        this.issuedDate = issuedDate;
        this.certUrl = certUrl;
    }
}
