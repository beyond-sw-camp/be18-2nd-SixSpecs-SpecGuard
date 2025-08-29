package com.beyond.specguard.auth.model.repository;

import com.beyond.specguard.auth.model.entity.ClientUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    //slug 기반 조회 추가
    Optional<ClientUser> findByEmailAndCompany_Slug(String email, String slug);
    //전체 slug
    List<ClientUser> findAllByCompany_Slug(String slug);
    //join fetch로 company까지 로딩 (로그인 시 사용)
    @Query("SELECT u FROM ClientUser u JOIN FETCH u.company WHERE u.email = :email")
    Optional<ClientUser> findByEmailWithCompany(@Param("email") String email);
}
