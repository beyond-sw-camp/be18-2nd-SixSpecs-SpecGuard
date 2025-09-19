package com.beyond.specguard.verification.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@lombok.experimental.SuperBuilder(toBuilder = true)
public abstract class EmailVerificationBase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private UUID id;

    @Column(nullable=false, length=255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=16)
    protected EmailVerifyStatus status = EmailVerifyStatus.PENDING;

    @Column(nullable=false)
    protected Integer attempts = 0;

    @Column(nullable=false)
    protected LocalDateTime lastRequestedAt;

    protected LocalDateTime verifiedAt;

    @Column(length=45)
    protected String lastIp;

    @Version
    private Long version;

    // === 템플릿 메소드 ===
    protected abstract boolean accountScopeFlag();
    protected abstract boolean hasScopeRef();
    protected abstract String scopeLabel();

    @PrePersist
    void prePersist() {
        if (lastRequestedAt == null) lastRequestedAt = LocalDateTime.now();
        if (status == null) status = EmailVerifyStatus.PENDING;
        if (attempts == null) attempts = 0;
        if (email != null) email = email.toLowerCase();
        validateScope();
    }

    @PreUpdate
    void preUpdate() {
        if (email != null) email = email.toLowerCase();
        validateScope();
    }

    private void validateScope() {
        if (accountScopeFlag() && hasScopeRef()) {
            throw new IllegalStateException("accountScope=true이면 " + scopeLabel() + "는 null이어야 합니다.");
        }
        if (!accountScopeFlag() && !hasScopeRef()) {
            throw new IllegalStateException("accountScope=false이면 " + scopeLabel() + "가 필요합니다.");
        }
    }
}
