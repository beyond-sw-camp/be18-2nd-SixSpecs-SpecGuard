package com.beyond.specguard.verification.model.repository;

import com.beyond.specguard.verification.model.entity.CompanyEmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface CompanyEmailVerificationRepo
        extends JpaRepository<CompanyEmailVerification, UUID> {
    Optional<CompanyEmailVerification> findByEmail(String email);
}
