package com.beyond.specguard.event;


import com.beyond.specguard.crawling.entity.CrawlingResult;
import com.beyond.specguard.crawling.repository.CrawlingResultRepository;
import com.beyond.specguard.githubcrawling.model.service.GitHubService;
import com.beyond.specguard.resume.model.entity.core.Resume;
import com.beyond.specguard.resume.model.entity.core.ResumeLink;
import com.beyond.specguard.resume.model.repository.ResumeLinkRepository;
import com.beyond.specguard.resume.model.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResumeSubmittedListener {

    private final GitHubService gitHubService;
    private final ResumeLinkRepository resumeLinkRepository;
    private final ResumeRepository resumeRepository;
    private final CrawlingResultRepository crawlingResultRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleResumeSubmitted(ResumeSubmittedEvent event) {
        UUID resumeId = event.resumeId();
        log.info("[AFTER_COMMIT] ResumeSubmittedEvent 수신 - resumeId={}", resumeId);

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalStateException("Resume not found: " + resumeId));

        List<ResumeLink> links = resumeLinkRepository.findByResume_Id(resumeId);

        for (ResumeLink link : links) {
            CrawlingResult result = crawlingResultRepository.findByResumeLink_Id(link.getId())
                    .orElseGet(() -> crawlingResultRepository.save(
                            CrawlingResult.builder()
                                    .resume(resume)
                                    .resumeLink(link)
                                    .crawlingStatus(CrawlingResult.CrawlingStatus.PENDING)
                                    .build()
                    ));

            switch (link.getLinkType()) {
                // 영속성 문제때문에 엔티티 그대로 넘기지 않고 id 뽑아서 호출
                case GITHUB -> gitHubService.analyzeGitHubUrl(result.getId());
                // case NOTION -> notionService.analyzeNotionUrl(result.getId());
                // case VELOG  -> velogService.analyzeVelogUrl(result.getId());
                default -> log.warn("지원하지 않는 링크 타입 - {}", link.getLinkType());
            }
        }
    }
}