package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.CompanyTemplateResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyTemplateResponseRepository extends JpaRepository<CompanyTemplateResponse, UUID> {
    Optional<CompanyTemplateResponse> findByResume_IdAndFieldId(UUID resumeId, UUID fieldId);

    List<CompanyTemplateResponse> findAllByResume_Id(UUID resumeId);

    List<CompanyTemplateResponse> findAllByResume_IdAndFieldIdIn(UUID resumeId, Collection<UUID> fieldIds);

    void deleteByResume_Id(UUID resumeId);
}
