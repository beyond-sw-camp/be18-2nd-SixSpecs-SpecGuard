package com.beyond.specguard.crawling.repository;

import com.beyond.specguard.crawling.entity.GitHubResumeSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface GitHubResumeSummaryRepository extends JpaRepository<GitHubResumeSummary, UUID> {
    @Query("select s from GitHubResumeSummary s where s.resume.id = :resumeId")
    Optional<GitHubResumeSummary> findByResumeId(UUID resumeId);
}