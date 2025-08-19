package com.beyond.specguard.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "applicant_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserEntity {

    @Id
    @Column(columnDefinition = "CHAR(36)", nullable = false, updatable = false)
    private String id;

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", length = 64, nullable = false)
    private String passwordHash;

    @Column(length = 50)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum Role {
        APPLICANT, ADMIN
    }
}
