package com.beyond.specguard.verification.model.repository;

import com.beyond.specguard.verification.model.entity.ApplicantEmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicantEmailVerificationRepo
        extends JpaRepository<ApplicantEmailVerification, Long> {
    Optional<ApplicantEmailVerification> findByEmail(String email);
}
