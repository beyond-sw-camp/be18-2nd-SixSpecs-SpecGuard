package com.beyond.specguard.crawling.repository;

import com.beyond.specguard.crawling.entity.GitHubResumeSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GitHubResumeSummaryRepository extends JpaRepository<GitHubResumeSummary, UUID> {

}