package com.beyond.specguard.githubcrawling.model.service;

import com.beyond.specguard.crawling.entity.CrawlingResult;
import com.beyond.specguard.crawling.entity.CrawlingResult.CrawlingStatus;
import com.beyond.specguard.crawling.repository.CrawlingResultRepository;
import com.beyond.specguard.githubcrawling.model.dto.GitHubStatsDto;
import com.beyond.specguard.githubcrawling.util.GitHubUrlParser;
import com.beyond.specguard.resume.model.entity.core.Resume;
import com.beyond.specguard.resume.model.entity.core.ResumeLink;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubService {

    private final CrawlingResultRepository crawlingResultRepository;
    private final GitHubApiClient gitHubApiClient;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void analyzeGitHubUrl(UUID resumeId, Resume resume, ResumeLink link) {
        // crawling_result row 확보 (없으면 생성)
        CrawlingResult result = crawlingResultRepository.findByResumeLink_Id(link.getId())
                .orElseGet(() -> crawlingResultRepository.save(
                        CrawlingResult.builder()
                                .resume(resume)
                                .resumeLink(link)
                                .crawlingStatus(CrawlingStatus.PENDING)
                                .build()
                ));

        try {
            result.updateStatus(CrawlingStatus.RUNNING);
            crawlingResultRepository.save(result);

            String username = GitHubUrlParser.extractUsername(link.getUrl());
            GitHubStatsDto stats = gitHubApiClient.fetchGitHubStats(username);

            result.updateContents(objectMapper.writeValueAsString(stats));
            result.updateStatus(CrawlingStatus.COMPLETED);

            log.info("✅ GitHub 크롤링 완료 - resumeId={}, url={}", resumeId, link.getUrl());
        } catch (Exception e) {
            result.updateStatus(CrawlingStatus.FAILED);
            log.error("❌ GitHub 크롤링 실패 - resumeId={}, url={}", resumeId, link.getUrl(), e);
        } finally {
            crawlingResultRepository.save(result);
        }
    }
}
