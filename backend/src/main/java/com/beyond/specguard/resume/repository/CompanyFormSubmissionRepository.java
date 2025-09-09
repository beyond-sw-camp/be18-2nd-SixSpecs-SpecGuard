package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.CompanyFormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompanyFormSubmissionRepository extends JpaRepository<CompanyFormSubmission, UUID> {
    boolean existsByResume_IdAndCompanyId(UUID resumeId, UUID companyId);
    Optional<CompanyFormSubmission> findTopByResume_IdOrderBySubmittedAtDesc(UUID resumeId);
}
