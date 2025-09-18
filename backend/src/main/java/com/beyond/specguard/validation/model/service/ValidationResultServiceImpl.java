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
import com.beyond.specguard.validation.model.entity.ValidationIssue;
import com.beyond.specguard.validation.model.entity.ValidationResult;
import com.beyond.specguard.validation.model.entity.ValidationResultLog;
import com.beyond.specguard.validation.model.repository.CalculateQueryRepository;
import com.beyond.specguard.validation.model.repository.ValidationIssueRepository;
import com.beyond.specguard.validation.model.repository.ValidationResultLogRepository;
import com.beyond.specguard.validation.model.repository.ValidationResultRepository;
import com.beyond.specguard.validation.util.KeywordUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;
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
    private final ValidationIssueRepository validationIssueRepository;

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



        try {
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


            //리포트용
            String reportJson = buildReportJson(
                    templateKeywords, ghKeywords, ghTech, notionKeywords, velogKeywords,
                    repoCount, commitCount, velogPostCount, velogDateCount,
                    githubRepoScore, githubCommitScore, githubKeywordMatch, githubTopicMatch,
                    notionKeywordMatch, velogKeywordMatch, velogPostScore, velogDateScore,
                    certScore, sourceDiversityFactor, finalScore, weights
            );



            validationResultLogRepository.save(
                    ValidationResultLog.builder()
                            .validationResult(result)
                            .validationScore(finalScore)
                            .keywordList(reportJson)
                            .mismatchFields(null)
                            .validatedAt(LocalDateTime.now())
                            .build()
            );

            // 8) 상태 전환
            resumeRepository.updateStatus(resumeId, Resume.ResumeStatus.VALIDATED);
            return result.getId();

        } catch (Exception ex) {
            log.error("Validation calculation failed for resumeId={} : {}", resumeId, ex.getMessage(), ex);
            return saveIssueAndLogsOnError(resumeId, ex);
        }
    }


    private UUID saveIssueAndLogsOnError(UUID resumeId, Exception ex) {
        // Issue 저장
        ValidationIssue issue = validationIssueRepository.save(
                ValidationIssue.builder()
                        .issueType(classifyIssueType(ex))
                        .issueDescription(truncate(ex.getMessage(), 2000))
                        .severity(ValidationIssue.Severity.HIGH)
                        .build()
        );

        // Result(0점) + 이슈 연결
        Resume resumeRef = em.getReference(Resume.class, resumeId);
        ValidationResult result = validationResultRepository.save(
                ValidationResult.builder()
                        .resume(resumeRef)
                        .validationIssue(issue)
                        .validationScore(0.0)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        // 로그 (오류 요약)
        String json = "{\"ERROR\":\"" + escapeJson(truncate(ex.getMessage(), 500)) + "\"}";
        validationResultLogRepository.save(
                ValidationResultLog.builder()
                        .validationResult(result)
                        .validationScore(0.0)
                        .keywordList(json)
                        .mismatchFields(null)
                        .validatedAt(LocalDateTime.now())
                        .descriptionComment(null)
                        .build()
        );
        // 실패 시 상태는 변경하지 않음
        return result.getId();
    }

    private ValidationIssue.IssueType classifyIssueType(Exception ex) {
        if (ex instanceof AuthenticationException) return ValidationIssue.IssueType.AUTH_API_FAILURE;
        if (ex instanceof AccessDeniedException)   return ValidationIssue.IssueType.COMPANY_MISMATCH;
        if (ex instanceof DataIntegrityViolationException) return ValidationIssue.IssueType.APPLICATION_VALUE_FORMAT_ERROR;
        if (ex instanceof JsonProcessingException) return ValidationIssue.IssueType.MORPHOLOGICAL_ANALYSIS_FAILURE;

        String msg = (ex.getMessage() == null) ? "" : ex.getMessage().toLowerCase();
        if (msg.contains("duplicate") || msg.contains("unique")) return ValidationIssue.IssueType.DUPLICATE_DATA_UPLOAD;
        if (msg.contains("not found") || msg.contains("no such")) return ValidationIssue.IssueType.USER_IDENTIFICATION_FAILED;
        if (msg.contains("version")) return ValidationIssue.IssueType.APPLICATION_VERSION_MISMATCH;
        if (msg.contains("format") || msg.contains("parse") || msg.contains("numberformat")) return ValidationIssue.IssueType.APPLICATION_VALUE_FORMAT_ERROR;
        if (msg.contains("nan") || msg.contains("infinite")) return ValidationIssue.IssueType.MODEL_PREDICTION_UNSTABLE;

        return ValidationIssue.IssueType.INVALID_ANALYSIS_VALUE;
    }

    private String buildReportJson(
            Set<String> templateKeywords,
            Set<String> ghKeywords, Set<String> ghTech,
            Set<String> notionKeywords, Set<String> velogKeywords,
            int repoCount, int commitCount, int velogPostCount, int velogDateCount,
            double githubRepoScore, double githubCommitScore, double githubKeywordMatch, double githubTopicMatch,
            double notionKeywordMatch, double velogKeywordMatch, double velogPostScore, double velogDateScore,
            double certScore, double sourceDiversityFactor, double finalScore,
            List<CalculateQueryRepository.WeightRow> weights
    ) throws JsonProcessingException {

        // 가중치 맵핑
        Map<String, Double> wm = new HashMap<>();
        for (var w : weights) {
            wm.put(w.getWeightType(), Optional.ofNullable(w.getWeightValue()).orElse(0.0));
        }

        // Strength/Weakness (상위/하위 30%)
        Map<String, Double> flat = new LinkedHashMap<>();
        flat.put("github.repo_count", githubRepoScore);
        flat.put("github.commit_frequency", githubCommitScore);
        flat.put("github.topic_match", githubTopicMatch);
        flat.put("github.keyword_match", githubKeywordMatch);
        flat.put("notion.keyword_match", notionKeywordMatch);
        flat.put("velog.post_count", velogPostScore);
        flat.put("velog.recent_activity", velogDateScore);
        flat.put("velog.keyword_match", velogKeywordMatch);
        flat.put("others.certificate_match", certScore);

        var strengths = new ArrayList<String>();
        var weaknesses = new ArrayList<String>();
        computeStrengthsWeaknesses(flat, strengths, weaknesses);

        // PDF 설계 반영한 구조 (요약)
        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, Object> applicantInfo = Map.of(
                "applicantName", null,
                "jobPosting", null,
                "submissionDate", null,
                "portfolioLinks", List.of()
        );
        root.put("applicantInfo", applicantInfo);
        root.put("finalScore", Math.round(finalScore * 1000.0) / 10.0); // 0~100 스케일(소수1자리)
        root.put("percentileRank", null);
        root.put("sourceDiversityFactor", Math.round(sourceDiversityFactor * 100.0) / 100.0);

        Map<String, Object> categoryScores = new LinkedHashMap<>();
        categoryScores.put("github", Map.of(
                "repo_count",        Map.of("score", githubRepoScore,  "weight", wm.getOrDefault("GITHUB_REPO_COUNT", 0.0)),
                "commit_frequency",  Map.of("score", githubCommitScore,"weight", wm.getOrDefault("GITHUB_COMMIT_COUNT", 0.0)),
                "topic_match",       Map.of("score", githubTopicMatch, "weight", wm.getOrDefault("GITHUB_TOPIC_MATCH", 0.0)),
                "keyword_match",     Map.of("score", githubKeywordMatch,"weight", wm.getOrDefault("GITHUB_KEYWORD_MATCH", 0.0))
        ));
        categoryScores.put("notion", Map.of(
                "keyword_match", Map.of("score", notionKeywordMatch,"weight", wm.getOrDefault("NOTION_KEYWORD_MATCH", 0.0))
        ));
        categoryScores.put("velog", Map.of(
                "post_count",       Map.of("score", velogPostScore, "weight", wm.getOrDefault("VELOG_POST_COUNT", 0.0)),
                "recent_activity",  Map.of("score", velogDateScore, "weight", wm.getOrDefault("VELOG_RECENT_ACTIVITY", 0.0)),
                "keyword_match",    Map.of("score", velogKeywordMatch,"weight", wm.getOrDefault("VELOG_KEYWORD_MATCH", 0.0))
        ));
        categoryScores.put("others", Map.of(
                "certificate_match", Map.of("score", certScore,"weight", wm.getOrDefault("CERTIFICATE_MATCH", 0.0))
        ));
        root.put("categoryScores", categoryScores);

        root.put("strengths", strengths);
        root.put("weaknesses", weaknesses);

        String summary = "자동 요약은 추후 NLP 모듈 연동 시 생성됩니다. 현재는 지표 기반의 정량 결과만 반영됩니다.";
        root.put("summary", summary);

        // 원천 값도 첨부(디버깅/트레이싱)
        root.put("raw", Map.of(
                "TEMPLATE", Map.of("keywordsCount", templateKeywords.size()),
                "GITHUB",   Map.of("keywords", ghKeywords, "tech", ghTech, "repo", repoCount, "commits", commitCount),
                "NOTION",   Map.of("keywords", notionKeywords),
                "VELOG",    Map.of("keywords", velogKeywords, "postCount", velogPostCount, "dateCount", velogDateCount)
        ));

        return OM.writeValueAsString(root);
    }

    private void computeStrengthsWeaknesses(Map<String, Double> flat, List<String> strengths, List<String> weaknesses) {
        var values = new ArrayList<>(flat.values());
        values.sort(Double::compareTo);
        if (values.isEmpty()) return;
        double p30 = values.get((int)Math.floor(values.size() * 0.30));
        double p70 = values.get((int)Math.floor(values.size() * 0.70));
        for (var e : flat.entrySet()) {
            if (e.getValue() >= p70 && e.getValue() > 0) strengths.add(e.getKey());
            if (e.getValue() <= p30) weaknesses.add(e.getKey());
        }
    }

    private static double clamp01(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return 0.0;
        return Math.max(0.0, Math.min(1.0, v));
    }
    private static String truncate(String s, int max) {
        if (s == null) return null;
        return (s.length() <= max) ? s : s.substring(0, max);
    }
    private static String escapeJson(String s) {
        if (s == null) return null;
        return s.replace("\"","\\\"");
    }
}