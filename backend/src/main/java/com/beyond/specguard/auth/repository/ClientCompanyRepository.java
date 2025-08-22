package com.beyond.specguard.auth.repository;

import com.beyond.specguard.auth.entity.ClientCompany;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClientCompanyRepository extends JpaRepository<ClientCompany, UUID> {

    // 사업자번호 중복 가입 방지
    boolean existsByBusinessNumber(String businessNumber);

    // 슬러그(회사 고유 URL) 중복 체크
    boolean existsBySlug(String slug);

    // 회사 조회
    Optional<ClientCompany> findByBusinessNumber(String businessNumber);
    Optional<ClientCompany> findBySlug(String slug);
}
