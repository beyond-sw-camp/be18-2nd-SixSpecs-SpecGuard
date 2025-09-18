package com.beyond.specguard.validation.model.service;
import com.beyond.specguard.evaluationprofile.model.entity.EvaluationWeight;
import com.beyond.specguard.evaluationprofile.model.entity.EvaluationWeight.WeightType;
import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.common.exception.errorcode.CommonErrorCode;
import com.beyond.specguard.company.common.model.entity.ClientUser;
import com.beyond.specguard.resume.model.entity.Resume;
import com.beyond.specguard.resume.model.entity.ResumeLink;
import com.beyond.specguard.resume.model.repository.ResumeRepository;
import com.beyond.specguard.validation.model.dto.request.ValidationCalculateRequestDto;
import com.beyond.specguard.validation.model.entity.ValidationResult;
import com.beyond.specguard.validation.model.entity.ValidationResultLog;
import com.beyond.specguard.validation.model.repository.CalculateQueryRepository;
import com.beyond.specguard.validation.model.repository.ValidationResultLogRepository;
import com.beyond.specguard.validation.model.repository.ValidationResultRepository;
import com.beyond.specguard.validation.util.KeywordUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationResultServiceImpl implements ValidationResultService{
    private final ValidationResultRepository validationResultRepository;
    private final ValidationResultLogRepository validationResultLogRepository;
    private final CalculateQueryRepository calculateQueryRepository;
    private final ResumeRepository resumeRepository;

    @PersistenceContext
    private EntityManager em;

    private static final ObjectMapper OM = new ObjectMapper();

    // 정규화 상한
    private static final double REPO_MAX = 30.0;
    private static final double COMMIT_MAX = 1000.0;
    private static final double VELOG_POST_MAX = 100.0;

    private void validateWriteRole(ClientUser.Role role) {
        if (!EnumSet.of(ClientUser.Role.OWNER, ClientUser.Role.MANAGER).contains(role)) {
            throw new CustomException(CommonErrorCode.ACCESS_DENIED);
        }
    }

    @Override
    @Transactional
    public UUID calculateAndSave(ClientUser clientUser, ValidationCalculateRequestDto request) {
        validateWriteRole(clientUser.getRole());
        final UUID resumeId = request.getResumeId();

        // 1) 템플릿 키워드 수집 (company_template_response_analysis.keyword)
        List<String> templateKeywordJsons =
                calculateQueryRepository.findTemplateAnalysisKeywordsJson(resumeId);
        Set<String> templateKeywords = new LinkedHashSet<>();
        for (String json : templateKeywordJsons) {
            templateKeywords.addAll(KeywordUtils.parseKeywords(json));
        }

        // 2) 플랫폼별 포트폴리오 정제 결과 수집
        //    - keywords: 키워드 매칭
        //    - tech:     GitHub 토픽 매칭
        //    - count:    Velog 게시글 수
        Set<String> ghKeywords = new LinkedHashSet<>();     //깃허브 키워드
        Set<String> ghTech     = new LinkedHashSet<>();     //깃허브 기술 키워드
        for (String pc : calculateQueryRepository.findProcessedContentsByPlatform(resumeId, ResumeLink.LinkType.GITHUB.name())) {
            ghKeywords.addAll(KeywordUtils.parseKeywords(pc));
            ghTech.addAll(KeywordUtils.parseTech(pc));
        }

        Set<String> notionKeywords = new LinkedHashSet<>();    //노션 키워드
        for (String pc : calculateQueryRepository.findProcessedContentsByPlatform(resumeId, ResumeLink.LinkType.NOTION.name())) {
            notionKeywords.addAll(KeywordUtils.parseKeywords(pc));
        }

        Set<String> velogKeywords = new LinkedHashSet<>();      //벨로그 키워드
        int velogPostCount = 0;                                 //벨로그 개수
        int velogDateCount = 0;                                 //벨로그 최근 개수
        for (String pc : calculateQueryRepository.findProcessedContentsByPlatform(resumeId, ResumeLink.LinkType.VELOG.name())) {
            velogKeywords.addAll(KeywordUtils.parseKeywords(pc));
            velogPostCount += KeywordUtils.parseCount(pc);
            velogDateCount += KeywordUtils.dateCount(pc);
        }

        // 3) GitHub 메타데이터(레포/커밋) 합계
        var githubAgg = calculateQueryRepository.sumGithubStats(resumeId);
        int repoCount   = ((Number) githubAgg.getOrDefault("repoSum", 0)).intValue();   //레포 수
        int commitCount = ((Number) githubAgg.getOrDefault("commitSum", 0)).intValue(); //커밋 수

        // 4) 자격증 매칭 COMPLETED / (COMPLETED + FAILED)
        var certAgg = calculateQueryRepository.countCertificateVerification(resumeId);
        int completed = ((Number) certAgg.getOrDefault("completed", 0)).intValue();
        int failed    = ((Number) certAgg.getOrDefault("failed", 0)).intValue();
        double certScore = (completed + failed) == 0 ? 0.0 : (double) completed / (completed + failed);

        // 5) 지표 산출(0~1)
        double githubRepoScore      = clamp01(repoCount   / REPO_MAX);
        double githubCommitScore    = clamp01(commitCount / COMMIT_MAX);
        double githubKeywordMatch   = KeywordUtils.jaccard(ghKeywords, templateKeywords);
        double githubTopicMatch     = KeywordUtils.jaccard(ghTech,     templateKeywords);
        double notionKeywordMatch   = KeywordUtils.jaccard(notionKeywords, templateKeywords);
        double velogKeywordMatch    = KeywordUtils.jaccard(velogKeywords,  templateKeywords);
        double velogPostScore       = clamp01(velogPostCount / VELOG_POST_MAX);
        double velogDateScore     = clamp01(velogDateCount / VELOG_POST_MAX);

        // 6) 가중치 적용 (존재하는 지표만 합산)
        Map<WeightType, Double> metrics = new EnumMap<>(WeightType.class);
        metrics.put(WeightType.GITHUB_REPO_COUNT,     githubRepoScore);
        metrics.put(WeightType.GITHUB_COMMIT_COUNT,   githubCommitScore);
        metrics.put(WeightType.GITHUB_KEYWORD_MATCH,  githubKeywordMatch);
        metrics.put(WeightType.GITHUB_TOPIC_MATCH,    githubTopicMatch);
        metrics.put(WeightType.NOTION_KEYWORD_MATCH,  notionKeywordMatch);
        metrics.put(WeightType.VELOG_KEYWORD_MATCH,   velogKeywordMatch);
        metrics.put(WeightType.VELOG_POST_COUNT,      velogPostScore);
        metrics.put(WeightType.VELOG_RECENT_ACTIVITY,    velogDateScore);
        metrics.put(WeightType.CERTIFICATE_MATCH,     certScore);



        //가중치
        var weights = calculateQueryRepository.findWeightsByResume(resumeId);


        //정합성 점수 종합
        int sourceCount = 9;
        if(githubRepoScore == 0.0){sourceCount--;}
        if(githubCommitScore == 0.0){sourceCount--;}
        if(githubKeywordMatch == 0.0){sourceCount--;}
        if(githubTopicMatch == 0.0){sourceCount--;}
        if(notionKeywordMatch == 0.0){sourceCount--;}
        if(velogKeywordMatch == 0.0){sourceCount--;}
        if(velogPostScore == 0.0){sourceCount--;}
        if(velogDateScore == 0.0){sourceCount--;}
        if(certScore == 0.0){sourceCount--;}
        double sourceDiversityFactor = Math.log(1+sourceCount) / Math.log(9);

        double rawTotal = 0.0;
        for(var w : weights){
            WeightType wt;
            try{
                wt = WeightType.valueOf(w.getWeightType());
            }catch(Exception e){
                throw new IllegalArgumentException("Invalid weight type: " + w.getWeightType());
            }
            if(!metrics.containsKey(wt)){
                continue;
            }
            double weight = Optional.ofNullable(w.getWeightValue()).orElse(0.0);
            double score  = metrics.get(wt);
            rawTotal += weight * score;
        }

        double adjustedTotal = rawTotal * sourceDiversityFactor;
        //TODO percentile 계산
        double finalScore = adjustedTotal * 100;




        // 7) 저장 (빌더만)
        Resume resumeRef = em.getReference(Resume.class, resumeId);
        ValidationResult result = validationResultRepository.save(
                ValidationResult.builder()
                        .resume(resumeRef)
                        .validationScore(finalScore)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        // 로그용 요약(JSON) –
        String keywordListJson;
        try {
            Map<String,Object> summary = new LinkedHashMap<>();
            summary.put("TEMPLATE", Map.of("keywordsCount", templateKeywords.size()));
            summary.put("GITHUB",  Map.of("keywords", ghKeywords, "tech", ghTech, "repo", repoCount, "commits", commitCount));
            summary.put("NOTION",  Map.of("keywords", notionKeywords));
            summary.put("VELOG",   Map.of("keywords", velogKeywords, "postCount", velogPostCount, "dateCount", velogDateCount));
            summary.put("SCORES",  Map.of(
                    "GITHUB_REPO_COUNT", githubRepoScore,
                    "GITHUB_COMMIT_COUNT", githubCommitScore,
                    "GITHUB_KEYWORD_MATCH", githubKeywordMatch,
                    "GITHUB_TOPIC_MATCH", githubTopicMatch,
                    "NOTION_KEYWORD_MATCH", notionKeywordMatch,
                    "VELOG_KEYWORD_MATCH", velogKeywordMatch,
                    "VELOG_POST_COUNT", velogPostScore,
                    "VELOG_RECENT_ACTIVITY", velogDateScore,
                    "CERTIFICATE_MATCH", certScore,
                    "FINAL", finalScore
            ));
            keywordListJson = OM.writeValueAsString(summary);
        } catch (Exception e) {
            keywordListJson = "{\"SCORES\":{\"FINAL\":"+finalScore+"}}";
        }

        validationResultLogRepository.save(
                ValidationResultLog.builder()
                        .validationResult(result)
                        .validationScore(finalScore)
                        .keywordList(keywordListJson)
                        .mismatchFields(null)
                        .validatedAt(LocalDateTime.now())
                        .build()
        );

        // 8) 상태 전환 COMPLETED → VALIDATED
        resumeRepository.updateStatus(resumeId, Resume.ResumeStatus.VALIDATED);

        return result.getId();
    }

    //정규화 함수 0.0~1.0 강제
    private static double clamp01(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return 0.0;
        return Math.max(0.0, Math.min(1.0, v));
    }
}
