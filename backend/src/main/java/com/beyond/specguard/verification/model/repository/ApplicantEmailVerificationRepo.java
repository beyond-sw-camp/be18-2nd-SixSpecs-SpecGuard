package com.beyond.specguard.verification.model.repository;

import com.beyond.specguard.verification.model.entity.ApplicantEmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApplicantEmailVerificationRepo
        extends JpaRepository<ApplicantEmailVerification, UUID> {
    Optional<ApplicantEmailVerification> findByEmailAndResumeId(String email, UUID resumeId);
}
