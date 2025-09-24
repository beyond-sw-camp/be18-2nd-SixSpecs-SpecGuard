package com.beyond.specguard.event.listener;

import com.beyond.specguard.event.ResumeSubmittedEvent;
import com.beyond.specguard.event.client.TemplateNlpClient;
import com.beyond.specguard.event.dto.BaseResponse;
import com.beyond.specguard.event.dto.KeywordRequest;
import com.beyond.specguard.event.dto.KeywordResponse;
import com.beyond.specguard.event.dto.SummaryRequest;
import com.beyond.specguard.event.dto.SummaryResponse;
import com.beyond.specguard.resume.model.entity.CompanyTemplateResponse;
import com.beyond.specguard.resume.model.entity.CompanyTemplateResponseAnalysis;
import com.beyond.specguard.resume.model.repository.CompanyTemplateResponseAnalysisRepository;
import com.beyond.specguard.resume.model.repository.CompanyTemplateResponseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResumeTemplateAnalysisListener {

    private final CompanyTemplateResponseRepository responseRepository;
    private final CompanyTemplateResponseAnalysisRepository analysisRepository;
    private final TemplateNlpClient nlpClient;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleResumeSubmitted(ResumeSubmittedEvent event) {
        UUID resumeId = event.resumeId();
        log.info("[NLP] 자소서 요약 및 키워드 추출 분석 시작 resumeId={}", resumeId);

        List<CompanyTemplateResponse> responses =
                responseRepository.findAllByResume_Id(resumeId);

        for (CompanyTemplateResponse response : responses) {
            if (response.getAnswer() == null || response.getAnswer().isBlank()) {
                continue;
            }

            try {
                // 1) 요약 요청
                SummaryRequest summaryReq = new SummaryRequest("cover_letter", response.getAnswer());
                BaseResponse<SummaryResponse> summaryRes = nlpClient.summarize(summaryReq);

                // 2) 키워드 요청
                KeywordRequest keywordReq = new KeywordRequest("portfolio", response.getAnswer());
                BaseResponse<KeywordResponse> keywordRes = nlpClient.extractKeywords(keywordReq);

                // 3) 결과 저장 (없으면 insert, 있으면 update)
                CompanyTemplateResponseAnalysis analysis = analysisRepository
                        .findByResponseId(response.getId())
                        .orElseGet(() -> CompanyTemplateResponseAnalysis.builder()
                                .responseId(response.getId())
                                .build());

                String summary = summaryRes.getData().getSummary();

                // 키워드 저장 (List<String> → JSON 문자열, null-safe)
                List<String> keywords = Optional.ofNullable(keywordRes.getData())
                        .map(KeywordResponse::getKeywords)
                        .orElse(List.of());

                String keywordsJson = new ObjectMapper().writeValueAsString(
                        Map.of("keywords", keywords)
                );

                // 분석 업데이트
                analysis.updateAnalysis(summary, keywordsJson);
                analysisRepository.save(analysis);

                log.info("[NLP] 분석 완료 responseId={}", response.getId());

            } catch (Exception e) {
                log.error("[NLP] 분석 실패 responseId={}", response.getId(), e);
            }
        }
    }
}
