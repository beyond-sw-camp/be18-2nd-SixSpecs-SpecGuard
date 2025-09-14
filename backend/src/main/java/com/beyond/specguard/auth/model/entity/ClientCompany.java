package com.beyond.specguard.auth.model.entity;

import com.beyond.specguard.company.management.model.dto.request.UpdateCompanyRequestDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "client_company")
@Getter  // 꼭 추가!
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ClientUser> users = new ArrayList<>();

    public void update(UpdateCompanyRequestDto dto) {
        if (dto.getName() != null) this.name = dto.getName();
        if (dto.getManagerName() != null) this.managerName = dto.getManagerName();
        if (dto.getManagerPosition() != null) this.managerPosition = dto.getManagerPosition();
        if (dto.getContactEmail() != null) this.contactEmail = dto.getContactEmail();
        if (dto.getContactMobile() != null) this.contactMobile = dto.getContactMobile();
    }

}
