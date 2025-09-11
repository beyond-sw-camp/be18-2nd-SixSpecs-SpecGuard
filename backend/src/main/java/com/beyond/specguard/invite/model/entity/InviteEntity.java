package com.beyond.specguard.invite.model.entity;

import com.beyond.specguard.auth.model.entity.ClientCompany;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_invite")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class InviteEntity {

    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "company_id",
            nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT) // FK 제약 제거
    )
    private ClientCompany company;

    @Column(nullable = false, length = 100)
    private String email;

    @NotNull
    @Enumerated(EnumType.STRING)
    private InviteRole role; // OWNER / MANAGER / VIEWER

    @Column(name = "invite_token", nullable = false, length = 255, unique = true)
    private String inviteToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InviteStatus status;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.expiresAt == null) {
            this.expiresAt = LocalDateTime.now().plusDays(7); // 기본 7일 유효
        }
        if (this.status == null) {
            this.status = InviteStatus.PENDING;
        }
    }

    public enum InviteRole {
        OWNER,
        MANAGER,
        VIEWER
    }

    public enum InviteStatus {
        PENDING,   // 초대 발송 후 아직 수락 안함
        ACCEPTED,  // 수락 완료
        EXPIRED    // 만료됨
    }
    public void inviteExpired() {
        this.status = InviteStatus.EXPIRED;
    }

    public void inviteAccepted() {
        this.status = InviteStatus.ACCEPTED;
    }
}
