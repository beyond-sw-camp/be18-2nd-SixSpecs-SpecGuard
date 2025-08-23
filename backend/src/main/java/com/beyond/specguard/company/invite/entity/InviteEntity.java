package com.beyond.specguard.company.invite.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_invite")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class InviteEntity {

    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "company_id", nullable = false, columnDefinition = "CHAR(36)")
    private String companyId;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String role; // OWNER / MANAGER / VIEWER

    @Column(name = "invite_token", nullable = false, length = 255)
    private String inviteToken;

    @Column(name = "is_used", nullable = false)
    private boolean isUsed = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.inviteToken == null) {
            this.inviteToken = UUID.randomUUID().toString();
        }
    }
}
