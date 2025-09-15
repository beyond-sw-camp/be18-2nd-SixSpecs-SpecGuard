package com.beyond.specguard.resume.model.repository;

import com.beyond.specguard.resume.model.entity.CompanyTemplateResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompanyTemplateResponseRepository extends JpaRepository<CompanyTemplateResponse, UUID> {
    Optional<CompanyTemplateResponse> findByResume_IdAndFieldId(UUID resumeId, UUID fieldId);
    void deleteByResume_Id(UUID resumeId);

    Optional<CompanyTemplateResponse> findByResume_IdAndField_Id(UUID resumeId, UUID fieldId);
}
