package com.beyond.specguard.verification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.annotation.Id;

import java.time.Instant;

@Entity
@Table(name = "phone_verification")
@Getter
@Setter
public class PhoneVerification {

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = "PENDING";
    }

    @jakarta.persistence.Id
    @Column(length = 36)
    private String id; // UUIDv7

    @Column(length = 36)
    private String userId;

    @Column(length = 20, nullable = false)
    private String phone;

    @Column(length = 32, nullable = false, unique = true)
    private String token; // e.g. VERIF-9F3A2CDFS23UF89ASD

    @Column(length = 16, nullable = false)
    private String channel; // EMAIL_SMSTO | NUMBER_SMSTO

    @Column(length = 16, nullable = false)
    private String status; // PENDING | SUCCESS | FAIL | EXPIRED

    @Column(length = 100)
    private String payloadRef;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant usedAt;

    private Instant verifiedAt;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();


}
