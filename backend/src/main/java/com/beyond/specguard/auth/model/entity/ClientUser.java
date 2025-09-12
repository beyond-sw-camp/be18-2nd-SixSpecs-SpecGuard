package com.beyond.specguard.auth.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "client_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ClientUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    // FK 조건 서비스단에서 검증하도록 수정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "company_id",
            nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT) // FK 제약 제거
    )
    private ClientCompany company;

    // 사용자 이름 (nullable 허용 → 소셜 로그인 시 내려줄 수도 있음)
    @Column(length = 100, nullable = true)
    private String name;

    // 초대 이메일 = 로그인 이메일 (엄격 모드 기준, UNIQUE)
    @Column(length = 255, nullable = false, unique = true)
    private String email;

    // 로컬 로그인일 경우에만 값 존재
    @Column(name = "password_hash", length = 64, nullable = true)
    private String passwordHash;

    @Column(length = 50)
    private String phone;

    // 권한 (OWNER / MANAGER / VIEWER)
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;


    // 소셜 로그인용
    @Column(length = 20, nullable = false)
    private String provider;   // local / google / naver

    @Column(name = "provider_id", length = 100, nullable = true)
    private String providerId; // 소셜 계정 고유 ID (구글 sub, 네이버 id)

    @Column(name = "profile_image", length = 500, nullable = true)
    private String profileImage;

    public enum Role {
        OWNER, MANAGER, VIEWER
    }
}
