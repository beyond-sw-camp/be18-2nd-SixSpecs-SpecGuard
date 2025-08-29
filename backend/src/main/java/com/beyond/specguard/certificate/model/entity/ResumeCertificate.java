package com.beyond.specguard.certificate.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "resume_certificate")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeCertificate {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id; // UUID


    // TODO 차후 resume과 연결
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "resume_id", nullable = false)
    @Column(name = "resume_id", nullable = false)
    private UUID resume;

    @Column(name = "certificate_name", nullable = false)
    private String certificateName;

    @Column(name = "certificate_number", nullable = false)
    private String certificateNumber;

    @Column(name = "issuer", nullable = false)
    private String issuer;

    @Column(name = "issued_date", nullable = false)
    private LocalDate issuedDate;

    @Column(name = "cert_url")
    private String certUrl;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
