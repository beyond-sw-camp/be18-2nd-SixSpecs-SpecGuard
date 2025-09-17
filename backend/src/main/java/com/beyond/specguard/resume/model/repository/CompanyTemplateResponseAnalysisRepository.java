package com.beyond.specguard.resume.model.repository;

import com.beyond.specguard.resume.model.entity.core.CompanyTemplateResponseAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyTemplateResponseAnalysisRepository extends JpaRepository<CompanyTemplateResponseAnalysis, UUID> {

    Optional<CompanyTemplateResponseAnalysis> findByResponseId(UUID responseId);

    // ✅ resumeId 기준으로 analysis 전부 조회
    @Query("""
           SELECT a
           FROM CompanyTemplateResponseAnalysis a
           JOIN a.response r
           WHERE r.resume.id = :resumeId
           """)
    List<CompanyTemplateResponseAnalysis> findAllByResumeId(@Param("resumeId") UUID resumeId);
}
