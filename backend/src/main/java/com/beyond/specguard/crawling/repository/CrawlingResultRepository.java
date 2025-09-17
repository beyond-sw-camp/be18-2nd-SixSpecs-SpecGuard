package com.beyond.specguard.crawling.repository;

import com.beyond.specguard.crawling.entity.CrawlingResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CrawlingResultRepository extends JpaRepository<CrawlingResult, UUID> {

    // 특정 이력서 기준으로 결과 찾기
    List<CrawlingResult> findByResume_Id(UUID resumeId);

    // ResumeLink 기준으로 결과 찾기
    Optional<CrawlingResult> findByResumeLink_Id(UUID resumeLinkId);

    // 상태별 조회
    List<CrawlingResult> findByCrawlingStatus(CrawlingResult.CrawlingStatus status);
}
