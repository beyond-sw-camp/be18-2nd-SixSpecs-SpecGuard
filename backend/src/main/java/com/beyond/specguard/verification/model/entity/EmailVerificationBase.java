package com.beyond.specguard.verification.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public abstract class EmailVerificationBase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=16)
    private EmailVerifyStatus status = EmailVerifyStatus.PENDING;

    @Column(nullable=false)
    private Integer attempts = 0;

    @Column(nullable=false)
    private LocalDateTime lastRequestedAt;

    private LocalDateTime verifiedAt;

    @Column(length=45)
    private String lastIp;

    @Version
    private Long version;

    @PrePersist
    void prePersist() {
        if (lastRequestedAt == null) lastRequestedAt = LocalDateTime.now();
        if (status == null) status = EmailVerifyStatus.PENDING;
        if (attempts == null) attempts = 0;
        if (email != null) email = email.toLowerCase();
    }

    @PreUpdate
    void preUpdate() {
        if (email != null) email = email.toLowerCase();
    }
}

