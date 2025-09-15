package com.beyond.specguard.resume.model.entity.core;

import com.beyond.specguard.resume.model.dto.request.ResumeCertificateUpsertRequest;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "resume_certificate"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ResumeCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID id;

    //다대일
    //resume_id는 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false, columnDefinition = "CHAR(36)", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
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

    @CreationTimestamp
    @Column(name ="created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void update(ResumeCertificateUpsertRequest req) {
        if (req.certificateName() != null) this.certificateName = req.certificateName();
        if (req.certificateNumber() != null) this.certificateNumber = req.certificateNumber();
        if (req.issuer() != null) this.issuer = req.issuer();
        if (req.issuedDate() != null) this.issuedDate = req.issuedDate();
        if (req.certUrl() != null) this.certUrl = req.certUrl();
    }
}
