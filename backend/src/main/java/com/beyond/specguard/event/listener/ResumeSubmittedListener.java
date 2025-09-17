package com.beyond.specguard.event.listener;

import com.beyond.specguard.crawling.entity.CrawlingResult;
import com.beyond.specguard.crawling.repository.CrawlingResultRepository;
import com.beyond.specguard.event.client.PythonCrawlerClient;
import com.beyond.specguard.event.ResumeSubmittedEvent;
import com.beyond.specguard.githubcrawling.model.service.GitHubService;
import com.beyond.specguard.resume.model.entity.common.enums.LinkType;
import com.beyond.specguard.resume.model.entity.core.Resume;
import com.beyond.specguard.resume.model.entity.core.ResumeLink;
import com.beyond.specguard.resume.model.repository.ResumeLinkRepository;
import com.beyond.specguard.resume.model.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResumeSubmittedListener {

    private final GitHubService gitHubService;
    private final ResumeLinkRepository resumeLinkRepository;
    private final ResumeRepository resumeRepository;
    private final CrawlingResultRepository crawlingResultRepository;
    private final PythonCrawlerClient pythonCrawlerClient; // ✅ Python API 호출

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleResumeSubmitted(ResumeSubmittedEvent event) {
        UUID resumeId = event.resumeId();
        log.info("[AFTER_COMMIT] ResumeSubmittedEvent 수신 - resumeId={}", resumeId);

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalStateException("Resume not found: " + resumeId));

        List<ResumeLink> links = resumeLinkRepository.findByResume_Id(resumeId);

        for (ResumeLink link : links) {
            CrawlingResult result = null;

            try {
                //  CrawlingResult 안전 조회/생성
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
                                // 다른 트랜잭션이 먼저 생성했음 동시성 터져버림 에러던지고 → 다시 조회
                                log.warn("동시성 충돌: resumeLinkId={} 이미 CrawlingResult 생성됨", link.getId());
                                return crawlingResultRepository.findByResumeLink_Id(link.getId())
                                        .orElseThrow(() -> new IllegalStateException("ResumeLink는 있는데 CrawlingResult 없음"));
                            }
                        });

                crawlingResultRepository.save(result);

                switch (link.getLinkType()) {
                    case GITHUB -> gitHubService.analyzeGitHubUrl(result.getId());

                    case VELOG -> {
                        log.info("[VELOG] Python API 호출 시작 resumeId={}, url={}", resumeId, link.getUrl());

                        Map<String, Object> velogData = pythonCrawlerClient.callVelogApi(resumeId, link.getUrl());

                        log.info("[VELOG] Python API 응답: {}", velogData);
                        // Python이 상태/contents 저장 담당 → Spring은 건드리지 않음
                    }

                    case NOTION -> {
                        // 추후 Notion API 연결 예정
                    }

                    default -> log.warn("지원하지 않는 링크 타입 - {}", link.getLinkType());
                }

            } catch (Exception e) {
                if (result != null) {
                    result.updateStatus(CrawlingResult.CrawlingStatus.FAILED);
                    crawlingResultRepository.save(result);
                }
                log.error("크롤링 실패 resumeId={}, url={}", resumeId, link.getUrl(), e);
            } finally {
                if (link.getLinkType() == LinkType.GITHUB && result != null) {
                    crawlingResultRepository.save(result);
                }
            }
        }
    }
}
