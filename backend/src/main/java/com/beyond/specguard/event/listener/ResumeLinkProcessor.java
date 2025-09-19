package com.beyond.specguard.event.listener;

import com.beyond.specguard.crawling.entity.CrawlingResult;
import com.beyond.specguard.crawling.repository.CrawlingResultRepository;
import com.beyond.specguard.event.client.PythonCrawlerClient;
import com.beyond.specguard.githubcrawling.model.service.GitHubService;
import com.beyond.specguard.notioncrawling.service.PublicNotionCrawlerService;
import com.beyond.specguard.resume.model.entity.Resume;
import com.beyond.specguard.resume.model.entity.ResumeLink;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResumeLinkProcessor {

    private final GitHubService gitHubService;
    private final CrawlingResultRepository crawlingResultRepository;
    private final PythonCrawlerClient pythonCrawlerClient;
    private final PublicNotionCrawlerService publicNotionCrawlerService;

    @Async
    public void processLinkAsync(Resume resume, ResumeLink link) {
        CrawlingResult result = null;
        try {
            result = crawlingResultRepository.findByResumeLink_Id(link.getId())
                    .orElseGet(() -> {
                        try {
                            return crawlingResultRepository.save(
                                    CrawlingResult.builder()
                                            .resume(resume)
                                            .resumeLink(link)
                                            .crawlingStatus(CrawlingResult.CrawlingStatus.PENDING)
                                            .build()
                            );
                        } catch (DataIntegrityViolationException e) {
                            log.warn("동시성 충돌: resumeLinkId={} 이미 CrawlingResult 생성됨", link.getId());
                            return crawlingResultRepository.findByResumeLink_Id(link.getId())
                                    .orElseThrow(() -> new IllegalStateException("ResumeLink는 있는데 CrawlingResult 없음"));
                        }
                    });

            switch (link.getLinkType()) {
                case GITHUB -> {
                    log.info("[GITHUB] GitHub 크롤링 시작 resumeId={}, url={}", resume.getId(), link.getUrl());
                    gitHubService.analyzeGitHubUrl(result.getId());
                }
                case VELOG -> {
                    log.info("[VELOG] Python API 호출 시작 resumeId={}, url={}", resume.getId(), link.getUrl());
                    Map<String, Object> velogData = pythonCrawlerClient.callVelogApi(resume.getId(), link.getUrl());
                    log.info("[VELOG] Python API 응답: {}", velogData);
                }
                case NOTION -> {
                    log.info("[NOTION] Notion API 호출 시작 resumeId={}, url={}", resume.getId(), link.getUrl());
                    publicNotionCrawlerService.crawlAndSaveWithLogging(resume.getId(),link.getId(),link.getUrl());
                }
                default -> log.warn("지원하지 않는 링크 타입 - {}", link.getLinkType());
            }

        } catch (Exception e) {
            if (result != null) {
                result.updateStatus(CrawlingResult.CrawlingStatus.FAILED);
                crawlingResultRepository.save(result);
            }
            log.error("크롤링 실패 resumeId={}, url={}", resume.getId(), link.getUrl(), e);
        }
    }
}
