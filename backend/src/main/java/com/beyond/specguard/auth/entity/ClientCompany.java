package com.beyond.specguard.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "client_company")
@Getter  // 꼭 추가!
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // UUID → DB에 문자열(CHAR(36))로 저장됨
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 12)
    private String businessNumber;

    @Column(length = 64)
    private String slug;

    @Column(length = 64)
    private String managerPosition;

    @Column(length = 30)
    private String managerName;

    @Column(length = 100)
    private String contactMobile;

    @Column(length = 100)
    private String contactEmail;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClientUser> users = new ArrayList<>();
}
