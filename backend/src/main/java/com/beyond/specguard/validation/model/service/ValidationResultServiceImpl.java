package com.beyond.specguard.validation.model.service;

import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.common.exception.errorcode.CommonErrorCode;
import com.beyond.specguard.company.common.model.entity.ClientUser;
import com.beyond.specguard.evaluationprofile.model.entity.EvaluationWeight.WeightType;
import com.beyond.specguard.resume.model.entity.Resume;
import com.beyond.specguard.resume.model.entity.ResumeLink;
import com.beyond.specguard.resume.model.repository.ResumeRepository;
import com.beyond.specguard.validation.exception.errorcode.ValidationErrorCode;
import com.beyond.specguard.validation.model.dto.request.ValidationCalculateRequestDto;
import com.beyond.specguard.validation.model.dto.request.ValidationPercentileRequestDto;
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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            List<String> templateKeywordJsons = calculateQueryRepository.findTemplateAnalysisKeywordsJson(resumeId);
            Set<String> templateKeywords = new LinkedHashSet<>();
            for (String json : templateKeywordJsons) templateKeywords.addAll(KeywordUtils.parseKeywords(json));

            // 2) 플랫폼별 포트폴리오 정제 결과 수집
            Set<String> ghKeywords = new LinkedHashSet<>();     //깃허브 키워드
            Set<String> ghTech     = new LinkedHashSet<>();     //깃허브 기술 키워드
            int githubCommitCount = 0;                          //깃허브 커밋수
            int githubRepoCount = 0;                            //깃허브 레포수
            List<String> ghProcessed =
                    calculateQueryRepository.findProcessedContentsByPlatform(resumeId, ResumeLink.LinkType.GITHUB.name());
            for (String pc : ghProcessed) {
                ghKeywords.addAll(KeywordUtils.parseKeywords(pc));
                ghTech.addAll(KeywordUtils.parseTech(pc));
                githubCommitCount += KeywordUtils.commitCount(pc);
                githubRepoCount   += KeywordUtils.repoCount(pc);
            }

            Set<String> ghObserved = new LinkedHashSet<>(ghKeywords);
            ghObserved.addAll(ghTech);

            Set<String> notionKeywords = new LinkedHashSet<>();    //노션 키워드
            List<String> notionProcessed =
                    calculateQueryRepository.findProcessedContentsByPlatform(resumeId, ResumeLink.LinkType.NOTION.name());
            for (String pc : notionProcessed) {
                notionKeywords.addAll(KeywordUtils.parseKeywords(pc));
            }

            Set<String> velogKeywords = new LinkedHashSet<>();      //벨로그 키워드
            int velogPostCount = 0;                                 //벨로그 개수
            int velogDateCount = 0;                                 //벨로그 최근 개수
            List<String> velogProcessed =
                    calculateQueryRepository.findProcessedContentsByPlatform(resumeId, ResumeLink.LinkType.VELOG.name());
            for (String pc : velogProcessed) {
                velogKeywords.addAll(KeywordUtils.parseKeywords(pc));
                velogPostCount += KeywordUtils.parseCount(pc);
                velogDateCount += KeywordUtils.dateCount(pc);
            }


            // 4) 자격증 매칭 COMPLETED / (COMPLETED + FAILED)
            var certAgg   = calculateQueryRepository.countCertificateVerification(resumeId);
            int completed = ((Number) certAgg.getOrDefault("completed", 0)).intValue();
            int failed    = ((Number) certAgg.getOrDefault("failed", 0)).intValue();
            double certScore = (completed + failed) == 0 ? 0.0 : (double) completed / (completed + failed);

            // 5) 지표 산출(0~1)
            double githubRepoScore      = clamp01(githubRepoCount   / REPO_MAX);
            double githubCommitScore    = clamp01(githubCommitCount / COMMIT_MAX);
            double githubKeywordMatch   = KeywordUtils.jaccard(ghKeywords, templateKeywords);
            double githubTopicMatch     = KeywordUtils.jaccard(ghTech,     templateKeywords);
            double notionKeywordMatch   = KeywordUtils.jaccard(notionKeywords, templateKeywords);
            double velogKeywordMatch    = KeywordUtils.jaccard(velogKeywords,  templateKeywords);
            double velogPostScore       = clamp01(velogPostCount / VELOG_POST_MAX);
            double velogDateScore       = clamp01(velogDateCount / VELOG_POST_MAX);

            // 6) 가중치 적용 (존재하는 지표만 합산)
            Map<WeightType, Double> metrics = new EnumMap<>(WeightType.class);
            metrics.put(WeightType.GITHUB_REPO_COUNT,     githubRepoScore);
            metrics.put(WeightType.GITHUB_COMMIT_COUNT,   githubCommitScore);
            metrics.put(WeightType.GITHUB_KEYWORD_MATCH,  githubKeywordMatch);
            metrics.put(WeightType.GITHUB_TOPIC_MATCH,    githubTopicMatch);
            metrics.put(WeightType.NOTION_KEYWORD_MATCH,  notionKeywordMatch);
            metrics.put(WeightType.VELOG_KEYWORD_MATCH,   velogKeywordMatch);
            metrics.put(WeightType.VELOG_POST_COUNT,      velogPostScore);
            metrics.put(WeightType.VELOG_RECENT_ACTIVITY, velogDateScore);
            metrics.put(WeightType.CERTIFICATE_MATCH,     certScore);



            //가중치
            var weights = calculateQueryRepository.findWeightsByResume(resumeId);


            //정합성 점수 종합
            int sourceCount = 0;
            for (double v : new double[]{
                    githubRepoScore, githubCommitScore, githubKeywordMatch, githubTopicMatch,
                    notionKeywordMatch, velogKeywordMatch, velogPostScore, velogDateScore, certScore
            }) sourceCount += (v > 0.0 ? 1 : 0);
            double sourceDiversityFactor = (sourceCount == 0) ? 0.0 : Math.log(1 + sourceCount) / Math.log(9);

            double rawTotal = 0.0;
            for (var w : weights) {
                WeightType wt = WeightType.valueOf(w.getWeightType());
                if (!metrics.containsKey(wt)) continue;
                rawTotal += Optional.ofNullable(w.getWeightValue()).orElse(0.0) * metrics.get(wt);
            }

            double adjustedTotal = rawTotal * sourceDiversityFactor;

            Map<String, Integer> presenceScore = new HashMap<>();
            for (String k : templateKeywords) {
                int s = 0;
                if (ghObserved.contains(k))     s++;
                if (notionKeywords.contains(k)) s++;
                if (velogKeywords.contains(k))  s++;
                if (s > 0) presenceScore.put(k, s);
            }

            List<String> top5 = presenceScore.entrySet().stream()
                    .sorted((a, b) -> {
                        int byScore = Integer.compare(b.getValue(), a.getValue());
                        return (byScore != 0) ? byScore : a.getKey().compareTo(b.getKey());
                    })
                    .limit(5)
                    .map(Map.Entry::getKey)
                    .toList();

            String descriptionComment = top5.isEmpty() ? null : String.join(", ", top5);

            Set<String> observedUnion = new LinkedHashSet<>();
            observedUnion.addAll(ghObserved);
            observedUnion.addAll(notionKeywords);
            observedUnion.addAll(velogKeywords);

            Set<String> mismatch = new LinkedHashSet<>(templateKeywords);
            mismatch.removeAll(observedUnion);
            String mismatchJson = OM.writeValueAsString(mismatch);

            // 6) 성공 이슈(필수 FK) 생성
            ValidationIssue issue = validationIssueRepository.save(
                    ValidationIssue.builder()
                            .validationResult(ValidationIssue.ValidationResult.SUCCESS)
                            .build()
            );


            // 7) 저장 (빌더만)
            Resume resumeRef = em.getReference(Resume.class, resumeId);
            ValidationResult result = validationResultRepository.save(
                    ValidationResult.builder()
                            .resume(resumeRef)
                            .validationIssue(issue)
                            .adjustedTotal(adjustedTotal)
                            .build()
            );


            //리포트용
            String reportJson = buildReportJson(
                    templateKeywords, ghKeywords, ghTech, notionKeywords, velogKeywords,
                    githubRepoCount, githubCommitCount, velogPostCount, velogDateCount,
                    githubRepoScore, githubCommitScore, githubKeywordMatch, githubTopicMatch,
                    notionKeywordMatch, velogKeywordMatch, velogPostScore, velogDateScore,
                    certScore, sourceDiversityFactor, adjustedTotal, weights
            );

            validationResultLogRepository.save(
                    ValidationResultLog.builder()
                            .validationResult(result)
                            .validationScore(adjustedTotal)
                            .keywordList(reportJson)
                            .mismatchFields(mismatchJson)
                            .descriptionComment(descriptionComment)
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



    @Override
    @Transactional
    public UUID calculatePercentile(ClientUser clientUser, ValidationPercentileRequestDto request) {
        validateWriteRole(clientUser.getRole());
        final UUID templateId = request.getTemplateId();
        final UUID resumeId   = request.getResumeId();

        var population = validationResultRepository.findAllValidatedByTemplateId(templateId);
        if (population.isEmpty()) throw new CustomException(ValidationErrorCode.RESUME_NOT_FOUND);

        ValidationResult target = validationResultRepository.findByResumeId(resumeId)
                .orElseThrow(() -> new CustomException(ValidationErrorCode.RESUME_NOT_FOUND));
        if (target.getAdjustedTotal() == null) throw new CustomException(CommonErrorCode.INVALID_REQUEST);

        double x = target.getAdjustedTotal();
        int n = population.size(), less = 0, equal = 0;
        for (ValidationResult vr : population) {
            double v = Optional.ofNullable(vr.getAdjustedTotal()).orElse(0.0);
            if (v < x) less++;
            else if (Double.compare(v, x) == 0) equal++;
        }
        double percentile = (less + 0.5 * equal) / n;
        double finalScore = Math.max(0.0, Math.min(1.0, percentile)) * 100.0;

        validationResultRepository.updateFinalScore(target.getId(), finalScore);
        return target.getId();
    }



    private UUID saveIssueAndLogsOnError(UUID resumeId, Exception ex) {
        ValidationIssue issue = validationIssueRepository.save(
                ValidationIssue.builder()
                        .validationResult(classifyIssueType(ex))
                        .build()
        );
        Resume resumeRef = em.getReference(Resume.class, resumeId);
        ValidationResult result = validationResultRepository.save(
                ValidationResult.builder()
                        .resume(resumeRef)
                        .validationIssue(issue)
                        .adjustedTotal(0.0)
                        .build()
        );
        String json = "{\"ERROR\":\"" + escapeJson(truncate(ex.getMessage(), 500)) + "\"}";
        validationResultLogRepository.save(
                ValidationResultLog.builder()
                        .validationResult(result)
                        .validationScore(0.0)
                        .keywordList(json)
                        .validatedAt(LocalDateTime.now())
                        .build()
        );
        return result.getId();
    }

    private ValidationIssue.ValidationResult classifyIssueType(Exception ex) {
        return ValidationIssue.ValidationResult.FAILED;
    }


    private String buildReportJson(
            Set<String> templateKeywords,
            Set<String> ghKeywords, Set<String> ghTech,
            Set<String> notionKeywords, Set<String> velogKeywords,
            int repoCount, int commitCount, int velogPostCount, int velogDateCount,
            double githubRepoScore, double githubCommitScore, double githubKeywordMatch, double githubTopicMatch,
            double notionKeywordMatch, double velogKeywordMatch, double velogPostScore, double velogDateScore,
            double certScore, double sourceDiversityFactor, double adjustedTotal,
            List<CalculateQueryRepository.WeightRow> weights
    ) throws JsonProcessingException {
        ObjectNode root = OM.createObjectNode();


        ObjectNode kw = OM.createObjectNode();
        kw.set("template", toArray(templateKeywords));
        kw.set("github_keywords", toArray(ghKeywords));
        kw.set("github_topics", toArray(ghTech));
        kw.set("notion_keywords", toArray(notionKeywords));
        kw.set("velog_keywords", toArray(velogKeywords));
        root.set("keywords", kw);

        ObjectNode raw = OM.createObjectNode();
        raw.put("github_repo_count", repoCount);
        raw.put("github_commit_count", commitCount);
        raw.put("velog_post_count", velogPostCount);
        raw.put("velog_recent_activity", velogDateCount);
        root.set("raw_aggregates", raw);

        ObjectNode metrics = OM.createObjectNode();
        metrics.put("GITHUB_REPO_COUNT", githubRepoScore);
        metrics.put("GITHUB_COMMIT_COUNT", githubCommitScore);
        metrics.put("GITHUB_KEYWORD_MATCH", githubKeywordMatch);
        metrics.put("GITHUB_TOPIC_MATCH", githubTopicMatch);
        metrics.put("NOTION_KEYWORD_MATCH", notionKeywordMatch);
        metrics.put("VELOG_KEYWORD_MATCH", velogKeywordMatch);
        metrics.put("VELOG_POST_COUNT", velogPostScore);
        metrics.put("VELOG_RECENT_ACTIVITY", velogDateScore);
        metrics.put("CERTIFICATE_MATCH", certScore);
        root.set("metrics", metrics);

        ArrayNode ws = OM.createArrayNode();
        for (var w : weights) {
            ObjectNode n = OM.createObjectNode();
            n.put("weight_type", w.getWeightType());
            n.put("weight_value", Optional.ofNullable(w.getWeightValue()).orElse(0.0));
            ws.add(n);
        }
        root.set("weights", ws);

        ObjectNode summary = OM.createObjectNode();
        summary.put("source_diversity_factor", sourceDiversityFactor);
        summary.put("adjusted_total", adjustedTotal);
        root.set("summary", summary);

        return OM.writeValueAsString(root);
    }

    private static ArrayNode toArray(Collection<String> set) {
        ArrayNode arr = OM.createArrayNode();
        for (String s : set) arr.add(s);
        return arr;
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
        return s.replace("\"", "\\\"");
    }

}