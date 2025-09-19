package com.beyond.specguard.resume.model.repository;

import com.beyond.specguard.resume.model.entity.CompanyFormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CompanyFormSubmissionRepository extends JpaRepository<CompanyFormSubmission, UUID> {
    boolean existsByResume_IdAndCompanyId(UUID resumeId, UUID companyId);
    void deleteByResume_Id(UUID resumeId);
}
