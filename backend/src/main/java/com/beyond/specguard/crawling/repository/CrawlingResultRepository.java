package com.beyond.specguard.crawling.repository;

import com.beyond.specguard.crawling.entity.CrawlingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CrawlingResultRepository extends JpaRepository<CrawlingResult, UUID> {

    // 특정 이력서 기준으로 결과 찾기
    List<CrawlingResult> findByResume_Id(UUID resumeId);

    // ResumeLink 기준으로 결과 찾기
    Optional<CrawlingResult> findByResumeLink_Id(UUID resumeLinkId);

    // 최근에 업데이트 된 것만 조회하기
    @Query("select distinct r.resume.id " +
            "from CrawlingResult r " +
            "where r.updatedAt >= :cutoff")
    List<UUID> findUpdatedResumeIds(LocalDateTime cutoff);


}
