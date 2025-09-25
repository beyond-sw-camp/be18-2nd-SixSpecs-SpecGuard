package com.beyond.specguard.crawling.repository;

import com.beyond.specguard.crawling.entity.PortfolioResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioResultRepository extends JpaRepository<PortfolioResult, UUID> {

    Optional<PortfolioResult> findByCrawlingResultId(UUID crawlingResultId);

    // ✅ resumeId 로 모든 PortfolioResult 가져오기
    @Query("""
           SELECT p 
           FROM PortfolioResult p
           JOIN p.crawlingResult c
           WHERE c.resume.id = :resumeId
           """)
    List<PortfolioResult> findAllByResumeId(@Param("resumeId") UUID resumeId);
}