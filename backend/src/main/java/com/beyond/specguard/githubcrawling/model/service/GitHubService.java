package com.beyond.specguard.githubcrawling.model.service;

import com.beyond.specguard.crawling.entity.CrawlingResult;
import com.beyond.specguard.crawling.entity.CrawlingResult.CrawlingStatus;
import com.beyond.specguard.crawling.repository.CrawlingResultRepository;
import com.beyond.specguard.githubcrawling.exception.GitException;
import com.beyond.specguard.githubcrawling.exception.errorcode.GitErrorCode;
import com.beyond.specguard.githubcrawling.model.dto.GitHubStatsDto;
import com.beyond.specguard.githubcrawling.util.GitHubUrlParser;
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
    public void analyzeGitHubUrl(UUID resultId) {
        CrawlingResult result = crawlingResultRepository.findById(resultId)
                .orElseThrow(() -> new IllegalStateException("CrawlingResult not found: " + resultId));

        try {
            // URL 검증
            String username = GitHubUrlParser.extractUsername(result.getResumeLink().getUrl());
            if (username == null || username.isBlank()) {
                throw new GitException(GitErrorCode.GITHUB_INVALID_URL);
            }

            // GitHub API 호출
            GitHubStatsDto stats = gitHubApiClient.fetchGitHubStats(username);
            if (stats == null) {
                throw new GitException(GitErrorCode.GITHUB_API_ERROR);
            }

            // 응답 직렬화
            try {
                result.updateContents(objectMapper.writeValueAsString(stats));
            } catch (Exception e) {
                throw new GitException(GitErrorCode.GITHUB_PARSE_ERROR);
            }

            result.updateStatus(CrawlingStatus.COMPLETED);
            log.info(" GitHub 크롤링 완료 - resumeId={}, url={}",
                    result.getResume().getId(), result.getResumeLink().getUrl());

        } catch (GitException e) {
            result.updateStatus(CrawlingStatus.FAILED);
            crawlingResultRepository.save(result);
            log.error(" GitHub 크롤링 실패 - resumeId={}, url={}, code={}",
                    result.getResume().getId(), result.getResumeLink().getUrl(), e.getErrorCode().getCode(), e);
            throw e;

        } catch (Exception e) {
            result.updateStatus(CrawlingStatus.FAILED);
            crawlingResultRepository.save(result);
            log.error(" GitHub 크롤링 중 알 수 없는 오류 - resumeId={}, url={}",
                    result.getResume().getId(), result.getResumeLink().getUrl(), e);
            throw new GitException(GitErrorCode.GITHUB_UNKNOWN);

        } finally {
            crawlingResultRepository.save(result);
        }
    }
}
