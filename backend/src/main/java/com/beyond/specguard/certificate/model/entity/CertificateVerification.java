package com.beyond.specguard.certificate.model.entity;

import com.beyond.specguard.resume.model.entity.core.ResumeCertificate;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "certificate_verification")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id; // UUID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certificate_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ResumeCertificate resumeCertificate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "verification_source", length = 50)
    private String verificationSource;

    @Setter
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void setStatusSuccess() {
        this.status = Status.SUCCESS;
    }
    public void setStatusFailed() {
        this.status = Status.FAILED;
    }

    public void setVerifiedNow() {
        this.verifiedAt = LocalDateTime.now();
    }

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = Status.PENDING;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Status {
        PENDING, SUCCESS, FAILED
    }
}
