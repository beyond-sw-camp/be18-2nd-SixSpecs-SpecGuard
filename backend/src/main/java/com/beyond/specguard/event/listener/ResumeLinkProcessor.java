package com.beyond.specguard.event.listener;

import com.beyond.specguard.crawling.entity.CrawlingResult;
import com.beyond.specguard.crawling.repository.CrawlingResultRepository;
import com.beyond.specguard.event.client.VelogCrawlerClient;
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
    private final VelogCrawlerClient velogCrawlerClient;
    private final PublicNotionCrawlerService notionCrawlerService;

    @Async
    public void processLinkAsync(Resume resume, ResumeLink link) {
        long start = System.currentTimeMillis();
        String threadName = Thread.currentThread().getName();

        log.info("[Processor-START] resumeId={}, linkType={}, url={}, thread={}, startTime={}",
                resume.getId(), link.getLinkType(), link.getUrl(), threadName, start);

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
                    log.info("[GITHUB] GitHub 크롤링 시작 resumeId={}, url={}, thread={}",
                            resume.getId(), link.getUrl(), threadName);
                    gitHubService.analyzeGitHubUrl(result.getId());
                    log.info("[GITHUB] 크롤링 종료");
                }
                case VELOG -> {
                    log.info("[VELOG] Python API 호출 시작 resumeId={}, url={}, thread={}",
                            resume.getId(), link.getUrl(), threadName);
                    Map<String, Object> velogData = velogCrawlerClient.callVelogApi(resume.getId(), link.getUrl());
                    log.info("[VELOG] Python API 응답: {}, thread={}", velogData, threadName);
                }
                case NOTION -> {
                    log.info("[NOTION] Notion 크롤링 시작 resumeId={}, url={}, thread={}",
                            resume.getId(), link.getUrl(), threadName);
                    notionCrawlerService.crawlAndUpdate(result.getId(), link.getUrl());
                    log.info("[NOTION 크롤링 종료");
                }
                default -> log.warn("지원하지 않는 링크 타입 - {}", link.getLinkType());
            }

        } catch (Exception e) {
            if (result != null) {
                result.updateStatus(CrawlingResult.CrawlingStatus.FAILED);
                crawlingResultRepository.save(result);
            }
            log.error("크롤링 실패 resumeId={}, url={}, thread={}, error={}",
                    resume.getId(), link.getUrl(), threadName, e.getMessage(), e);
        } finally {
            long end = System.currentTimeMillis();
            log.info("[Processor-END] resumeId={}, linkType={}, url={}, thread={}, duration={}ms",
                    resume.getId(), link.getLinkType(), link.getUrl(), threadName, (end - start));
        }
    }
}
