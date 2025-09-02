package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.CompanyTemplateResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyTemplateResponseRepository extends JpaRepository<CompanyTemplateResponse, String> {

    List<CompanyTemplateResponse> findAllByResumeId(String resumeId);
    Optional<CompanyTemplateResponse> findByIdAndResumeId(String id, String resumeId);
    boolean existsByResumeIdAndFieldId(String resumeId, String fieldId);
    void deleteByIdAndResumeId(String id, String resumeId);
}
