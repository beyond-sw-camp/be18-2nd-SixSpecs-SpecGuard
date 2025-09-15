package com.beyond.specguard.verification.model.repository;

import com.beyond.specguard.verification.model.entity.CompanyEmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyEmailVerificationRepo
        extends JpaRepository<CompanyEmailVerification, Long> {
    Optional<CompanyEmailVerification> findByEmail(String email);
}
