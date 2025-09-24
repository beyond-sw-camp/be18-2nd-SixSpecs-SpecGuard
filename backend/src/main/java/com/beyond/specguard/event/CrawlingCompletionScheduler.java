package com.beyond.specguard.event;

import com.beyond.specguard.crawling.entity.CrawlingResult;
import com.beyond.specguard.crawling.entity.PortfolioResult;
import com.beyond.specguard.crawling.repository.CrawlingResultRepository;
import com.beyond.specguard.crawling.repository.PortfolioResultRepository;
import com.beyond.specguard.event.client.KeywordNlpClient;
import com.beyond.specguard.resume.model.entity.CompanyTemplateResponseAnalysis;
import com.beyond.specguard.resume.model.entity.Resume;
import com.beyond.specguard.resume.model.repository.CompanyTemplateResponseAnalysisRepository;
import com.beyond.specguard.resume.model.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

@Component
@RequiredArgsConstructor
@Slf4j
public class CrawlingCompletionScheduler {

    private final CrawlingResultRepository crawlingResultRepository;
    private final PortfolioResultRepository portfolioResultRepository;
    private final CompanyTemplateResponseAnalysisRepository analysisRepository;
    private final ResumeRepository resumeRepository;
    private final KeywordNlpClient keywordNlpClient;

    private final ReentrantLock lock = new ReentrantLock();

    // === 3분마다 실행: cutoff 기준 최근 변경된 Resume만 ===
    @Scheduled(fixedDelay = 210000)
    public void checkRecent() {
        if (!lock.tryLock()) {
            log.warn("[Recent] 이전 실행 중 → 스킵");
            return;
        }
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
            List<UUID> resumeIds = resumeRepository.findUnprocessedResumeIdsSince(cutoff);
            processResumes(resumeIds, "[Recent]");
        } finally {
            lock.unlock();
        }
    }

    // === 하루 한 번: 모든 미완료 Resume 전수조사 ===
    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
    public void fullScan() {
        lock.lock(); // 전수 조사가 하루에 한번은 무조건 발생해야 한다 => 정합성 보장
        try {
            List<UUID> resumeIds = resumeRepository.findAllByStatusNotIn(
                    List.of(Resume.ResumeStatus.PROCESSING, Resume.ResumeStatus.VALIDATED)
            );
            processResumes(resumeIds, "[FullScan]");
        } finally {
            lock.unlock();
        }
    }

    // === 공통 처리 로직 ===
    private void processResumes(List<UUID> resumeIds, String tag) {
        log.info("{} 처리 대상 Resume 수 = {}", tag, resumeIds.size());

        for (UUID resumeId : resumeIds) {
            Resume resume = resumeRepository.findById(resumeId)
                    .orElseThrow(() -> new IllegalStateException("Resume not found: " + resumeId));

            if (resume.getStatus() == Resume.ResumeStatus.PROCESSING) {
                log.debug("{} 이미 PROCESSING 상태이므로 skip: {}", tag, resumeId);
                continue;
            }

            List<CrawlingResult> results = crawlingResultRepository.findByResume_Id(resumeId);
            List<PortfolioResult> portfolioResults = portfolioResultRepository.findAllByResumeId(resumeId);
            List<CompanyTemplateResponseAnalysis> analyses = analysisRepository.findAllByResumeId(resumeId);

            updateResumeStatus(resume, results, portfolioResults, analyses);

            resumeRepository.save(resume);

            log.info("{} Resume 상태 갱신 완료: id={}, status={}", tag, resumeId, resume.getStatus());
        }
    }

    private void updateResumeStatus(Resume resume,
                                    List<CrawlingResult> results,
                                    List<PortfolioResult> portfolioResults,
                                    List<CompanyTemplateResponseAnalysis> analyses) {

        boolean allCrawlingCompleted = results.size() == 3 &&
                results.stream().allMatch(r ->
                        r.getCrawlingStatus() == CrawlingResult.CrawlingStatus.COMPLETED
                                || r.getCrawlingStatus() == CrawlingResult.CrawlingStatus.NOTEXISTED);

        boolean portfolioCompleted = (portfolioResults.size() == 3);

        boolean allNlpProcessed = analyses.stream()
                .allMatch(a -> a.getSummary() != null && !a.getSummary().isBlank());

        if (allCrawlingCompleted && !portfolioCompleted) {
            log.info("크롤링 완료 → NLP 실행: resumeId={}", resume.getId());
            keywordNlpClient.extractKeywords(resume.getId());

        } else if (allCrawlingCompleted && portfolioCompleted && allNlpProcessed) {
            resume.changeStatus(Resume.ResumeStatus.PROCESSING);
        }
    }
}
