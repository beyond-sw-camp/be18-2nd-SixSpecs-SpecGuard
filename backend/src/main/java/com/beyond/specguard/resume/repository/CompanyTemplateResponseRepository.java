package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.CompanyTemplateResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompanyTemplateResponseRepository extends JpaRepository<CompanyTemplateResponse, UUID> {
    Optional<CompanyTemplateResponse> findByResume_IdAndFieldId(UUID resumeId, UUID fieldId);
}
