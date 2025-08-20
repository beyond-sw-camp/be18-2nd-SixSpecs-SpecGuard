package com.beyond.specguard.auth.repository;

import com.beyond.specguard.auth.entity.RefreshEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshRepository extends JpaRepository<RefreshEntity, Long> {
    Optional<RefreshEntity> findByRefresh(String refresh);
    void deleteByUsername(String username);
    boolean existsByRefresh(String refreshToken);
    void deleteByRefresh(String refreshToken);
}
