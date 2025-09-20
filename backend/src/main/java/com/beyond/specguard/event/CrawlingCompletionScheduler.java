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
    private final KeywordNlpClient  keywordNlpClient;

    private final ReentrantLock lock = new ReentrantLock();

    @Scheduled(fixedDelay = 30000)
    public void checkCrawlingStatus() {
        if (!lock.tryLock()) {
            log.warn("이전 스케줄러 실행 중 → 이번 실행 스킵");
            return;
        }

        try {
            log.info("[Scheduler] Resume 상태 확인 시작");

            //  모든 resumeId distinct 추출
            List<UUID> resumeIds = crawlingResultRepository.findAll().stream()
                    .map(r -> r.getResume().getId())
                    .distinct()
                    .toList();

            for (UUID resumeId : resumeIds) {
                Resume resume = resumeRepository.findById(resumeId)
                        .orElseThrow(() -> new IllegalStateException("Resume not found: " + resumeId));

                //  resumeId 기준 CrawlingResult 전부 조회
                List<CrawlingResult> results = crawlingResultRepository.findByResume_Id(resumeId);

                // [NLP 호출 위치]
                keywordNlpClient.extractKeywords(resumeId);


                //  resumeId 기준 PortfolioResult 전부 조회 (한 번에)
                List<PortfolioResult> portfolioResults = portfolioResultRepository.findAllByResumeId(resumeId);

                //  resumeId 기준 Analysis 조회
                List<CompanyTemplateResponseAnalysis> analyses =
                        analysisRepository.findAllByResumeId(resumeId);

                // 상태 업데이트 호출
                updateResumeStatus(resume, results, portfolioResults, analyses);
                resumeRepository.save(resume);

                log.info("[Scheduler] Resume 상태 갱신 완료: resumeId={}, status={}",
                        resumeId, resume.getStatus());
            }

        } finally {
            lock.unlock();
        }
    }

    private void updateResumeStatus(Resume resume,
                                    List<CrawlingResult> results,
                                    List<PortfolioResult> portfolioResults,
                                    List<CompanyTemplateResponseAnalysis> analyses) {

        boolean anyRunning = results.stream()
                .anyMatch(r -> r.getCrawlingStatus() == CrawlingResult.CrawlingStatus.PENDING);
        // 모든 값의 합이 3개일때로 수정해야함.
        long completedOrNonExistedCount = results.stream()
                .filter(r -> r.getCrawlingStatus() == CrawlingResult.CrawlingStatus.COMPLETED
                        || r.getCrawlingStatus() == CrawlingResult.CrawlingStatus.NOTEXISTED)
                .count();

        boolean allCrawlingCompleted = (completedOrNonExistedCount == 3);

        //  합이 3개일때 수정해야함.
        long portfolioCompletedOrNonExisted = portfolioResults.stream()
                .filter(p -> p.getPortfolioStatus() == PortfolioResult.PortfolioStatus.COMPLETED
                        || p.getPortfolioStatus() == PortfolioResult.PortfolioStatus.NOTEXISTED)
                .count();
        boolean portfolioCompleted = (portfolioCompletedOrNonExisted == 3);

        //자소서 nlp 임 이건
        boolean allNlpProcessed = analyses.stream()
                .allMatch(a -> a.getSummary() != null && !a.getSummary().isBlank());

        if (anyRunning) {
            //  실행 중인 크롤링이 있으면 전체 상태는 PENDING
            resume.changeStatus(Resume.ResumeStatus.PENDING);
        } else if (allCrawlingCompleted && portfolioCompleted && allNlpProcessed) {
            //  전부 완료된 경우에 PROCESSING
            resume.changeStatus(Resume.ResumeStatus.PROCESSING);
        } else {
            // 애매한거 전부 PENDING
            resume.changeStatus(Resume.ResumeStatus.PENDING);
        }
    }
}