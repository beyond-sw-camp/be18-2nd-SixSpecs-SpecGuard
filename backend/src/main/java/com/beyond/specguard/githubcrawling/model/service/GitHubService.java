package com.beyond.specguard.githubcrawling.model.service; //전체 주석 처리

import com.beyond.specguard.crawling.entity.CrawlingResult;
import com.beyond.specguard.crawling.entity.CrawlingResult.CrawlingStatus;
import com.beyond.specguard.crawling.entity.GitHubResumeSummary;
import com.beyond.specguard.crawling.repository.CrawlingResultRepository;
import com.beyond.specguard.crawling.repository.GitHubResumeSummaryRepository;
import com.beyond.specguard.githubcrawling.exception.GitException;
import com.beyond.specguard.githubcrawling.exception.errorcode.GitErrorCode;
import com.beyond.specguard.githubcrawling.model.dto.GitHubStatsDto;
import com.beyond.specguard.githubcrawling.util.GitHubUrlParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubService {

    private final CrawlingResultRepository crawlingResultRepository;
    private final GitHubApiClient gitHubApiClient;
    private final ObjectMapper objectMapper;
    private final GitHubResumeSummaryRepository summaryRepository;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public GitHubStatsDto analyzeGitHubUrl(UUID resultId) {
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
                result.updateContents(objectMapper.writeValueAsString(stats).getBytes());
            } catch (Exception e) {
                throw new GitException(GitErrorCode.GITHUB_PARSE_ERROR);
            }

            String serialized = objectMapper.writeValueAsString(stats);

            byte[] compressed;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
                gzipOut.write(serialized.getBytes(StandardCharsets.UTF_8));
                gzipOut.finish();
                compressed = baos.toByteArray();
            }

// LONGBLOB 컬럼에 그대로 저장
            result.updateContents(compressed);

            // 5. CrawlingResult 업데이트
            result.updateContents(compressed);
            result.updateStatus(CrawlingResult.CrawlingStatus.COMPLETED);

            crawlingResultRepository.save(result);

            // 4. GitHubResumeSummary 저장
            GitHubResumeSummary summary = GitHubResumeSummary.builder()
                    .resume(result.getResume())
                    .repositoryCount(stats.getRepositoryCount())
                    .languageStats(stats.getLanguageStats())
                    .commitCount(stats.getCommitCount())
                    .build();
            summaryRepository.save(summary);


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
        return null;
    }
}
