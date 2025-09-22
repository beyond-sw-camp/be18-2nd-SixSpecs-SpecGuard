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
    private final KeywordNlpClient  keywordNlpClient;

    private final ReentrantLock lock = new ReentrantLock();

    @Scheduled(fixedDelay = 180000)
    public void checkCrawlingStatus() {
        if (!lock.tryLock()) {
            log.warn("이전 스케줄러 실행 중 → 이번 실행 스킵");
            return;
        }

        try {
            log.info("[Scheduler] Resume 상태 확인 시작");

            //  findAll은 전수조사라 리소스 많이 잡아먹어기때문에 processing이 아닌것만 하도록 수정
            //  List<UUID> resumeIds = resumeRepository.findUnprocessedResumeIds();

            // 얘는 최근 업데이트한걸 조회하고 그걸 여기 위로 올려서 검증을 수행하는거임
            LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30); //  30분으로 늘림
            List<UUID> resumeIds = resumeRepository.findUnprocessedResumeIdsSince(cutoff);

            log.info("[Scheduler] 최근 변경된 Resume 갯수={}", resumeIds.size());


            for (UUID resumeId : resumeIds) {
                Resume resume = resumeRepository.findById(resumeId)
                        .orElseThrow(() -> new IllegalStateException("Resume not found: " + resumeId));

                // 만약을 대비해서 여기에서 검증한번 더 진행
                if (resume.getStatus() == Resume.ResumeStatus.PROCESSING) {
                    log.debug("이미 PROCESSING 상태이므로 skip: resumeId={}", resumeId);
                    continue;
                }

                //  resumeId 기준 CrawlingResult,portfolioResult,Analaysis 전부 조회
                List<CrawlingResult> results = crawlingResultRepository.findByResume_Id(resumeId);
                List<PortfolioResult> portfolioResults = portfolioResultRepository.findAllByResumeId(resumeId);
                List<CompanyTemplateResponseAnalysis> analyses = analysisRepository.findAllByResumeId(resumeId);


                // NLP 호출까지 updateResumeStatus 안으로 이동
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

        // 지금 3으로 default로 걸어뒀는데 향후 확장성을 고려했을때 resume table에 colum 하나 추가해서 count를 넣는 방식으로 수정한다면 유동적으로 변경가능
        boolean allCrawlingCompleted = results.size() == 3 &&
                results.stream().allMatch(r ->
                        r.getCrawlingStatus() == CrawlingResult.CrawlingStatus.COMPLETED
                                || r.getCrawlingStatus() == CrawlingResult.CrawlingStatus.NOTEXISTED
                );
        boolean portfolioCompleted = (portfolioResults.size() == 3);
        boolean allNlpProcessed = analyses.stream()
                .allMatch(a -> a.getSummary() != null && !a.getSummary().isBlank());


        if (allCrawlingCompleted && !portfolioCompleted) {
            // 크롤링은 끝났는데 포트폴리오 결과 아직 없음 → 여기서 NLP 실행 (Python 트리거를 여기에 둬서 중복 호출 방지)
            log.info("크롤링 완료 → NLP(키워드 추출) 실행 resumeId={}", resume.getId());
            keywordNlpClient.extractKeywords(resume.getId());

        } else if (allCrawlingCompleted && portfolioCompleted && allNlpProcessed) {
            // 크롤링 완료 + 포트폴리오 결과 채워짐 + 자소서 NLP도 끝남 -> 최종 완료 상태
            resume.changeStatus(Resume.ResumeStatus.PROCESSING);

        } else {
            // 그 외는 다 PENDING
            resume.changeStatus(Resume.ResumeStatus.PENDING);
        }
    }
}