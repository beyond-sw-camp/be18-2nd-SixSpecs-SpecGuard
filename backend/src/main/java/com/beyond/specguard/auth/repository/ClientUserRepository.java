package com.beyond.specguard.auth.repository;

import com.beyond.specguard.auth.entity.ClientUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClientUserRepository extends JpaRepository<ClientUser, UUID> {

    // 특정 이메일이 전체 시스템에서 유일해야 할 경우
    boolean existsByEmail(String email);

    // 회사 단위로 이메일 중복 허용 정책일 경우
    boolean existsByEmailAndCompany_Id(String email, UUID companyId);

    // 로그인 / 인증 시 사용
    Optional<ClientUser> findByEmail(String email);

    // 회사별 유저 조회
    Optional<ClientUser> findByEmailAndCompany_Id(String email, UUID companyId);
}
