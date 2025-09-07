package com.beyond.specguard.auth.repository;

import com.beyond.specguard.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {
    boolean existsByEmail(String email);
    Optional<UserEntity> findByEmail(String email);
}
