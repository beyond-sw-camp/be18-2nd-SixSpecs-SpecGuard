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
import com.beyond.specguard.validation.model.dto.response.ValidationFinalSummaryResponseDto;
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
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationResultServiceImpl implements ValidationResultService {

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

    // ===== match/mismatch 유틸 =====
    private record MM(String matchCsv, String mismatchJson) {}

    // 템플릿 키워드(tpl)와 관측 키워드(obs)로 match/mismatch 계산
    private final BiFunction<Set<String>, Set<String>, MM> mm = (tpl, obs) -> {
        Set<String> matchSet = new LinkedHashSet<>(tpl);
        matchSet.retainAll(obs);

        Set<String> misSet = new LinkedHashSet<>(tpl);
        misSet.removeAll(obs);

        String matchCsv = String.join(", ", matchSet);
        String mismatchJson;
        try {
            mismatchJson = OM.writeValueAsString(misSet); // JSON 배열 문자열
        } catch (Exception e) {
            mismatchJson = "[]";
        }
        return new MM(matchCsv, mismatchJson);
    };

    private void validateWriteRole(ClientUser.Role role) {
        if (!EnumSet.of(ClientUser.Role.OWNER, ClientUser.Role.MANAGER).contains(role)) {
            throw new CustomException(ValidationErrorCode.FORBIDDEN);
        }
    }
    private void validateReadRole(ClientUser.Role role) {
        if (!EnumSet.of(ClientUser.Role.VIEWER, ClientUser.Role.OWNER, ClientUser.Role.MANAGER).contains(role)) {
            throw new CustomException(ValidationErrorCode.FORBIDDEN);
        }
    }

    @Override
    @Transactional
    public UUID calculateAndSave(ClientUser clientUser, ValidationCalculateRequestDto request) {
        validateWriteRole(clientUser.getRole());
        final UUID resumeId = request.getResumeId();

        try {
            // 1) 템플릿 키워드 수집
            List<String> templateKeywordJsons = calculateQueryRepository.findTemplateAnalysisKeywordsJson(resumeId);
            Set<String> templateKeywords = new LinkedHashSet<>();
            for (String json : templateKeywordJsons) templateKeywords.addAll(KeywordUtils.parseKeywords(json));

            // 2) 포트폴리오 정제 결과 수집
            Set<String> ghKeywords = new LinkedHashSet<>();
            Set<String> ghTech     = new LinkedHashSet<>();
            int githubCommitCount = 0;
            int githubRepoCount   = 0;
            for (String pc : calculateQueryRepository.findProcessedContentsByPlatform(resumeId, ResumeLink.LinkType.GITHUB.name())) {
                ghKeywords.addAll(KeywordUtils.parseKeywords(pc));
                ghTech.addAll(KeywordUtils.parseTech(pc));
                githubCommitCount += KeywordUtils.commitCount(pc);
                githubRepoCount   += KeywordUtils.repoCount(pc);
            }

            Set<String> notionKeywords = new LinkedHashSet<>();
            for (String pc : calculateQueryRepository.findProcessedContentsByPlatform(resumeId, ResumeLink.LinkType.NOTION.name())) {
                notionKeywords.addAll(KeywordUtils.parseKeywords(pc));
            }

            Set<String> velogKeywords = new LinkedHashSet<>();
            int velogPostCount = 0, velogDateCount = 0;
            for (String pc : calculateQueryRepository.findProcessedContentsByPlatform(resumeId, ResumeLink.LinkType.VELOG.name())) {
                velogKeywords.addAll(KeywordUtils.parseKeywords(pc));
                velogPostCount += KeywordUtils.parseCount(pc);
                velogDateCount += KeywordUtils.dateCount(pc);
            }

            // 3) 자격증 매칭 COMPLETED / (COMPLETED + FAILED)
            var certAgg = calculateQueryRepository.countCertificateVerification(resumeId);
            int completed = ((Number) certAgg.getOrDefault("completed", 0)).intValue();
            int failed    = ((Number) certAgg.getOrDefault("failed", 0)).intValue();
            double certScore = (completed + failed) == 0 ? 0.0 : (double) completed / (completed + failed);

            // 4) 지표 산출(0~1)
            double githubRepoScore      = clamp01(githubRepoCount   / REPO_MAX);
            double githubCommitScore    = clamp01(githubCommitCount / COMMIT_MAX);
            double githubKeywordMatch   = KeywordUtils.jaccard(ghKeywords, templateKeywords);
            double githubTopicMatch     = KeywordUtils.jaccard(ghTech,     templateKeywords);
            double notionKeywordMatch   = KeywordUtils.jaccard(notionKeywords, templateKeywords);
            double velogKeywordMatch    = KeywordUtils.jaccard(velogKeywords,  templateKeywords);
            double velogPostScore       = clamp01(velogPostCount / VELOG_POST_MAX);
            double velogDateScore       = clamp01(velogDateCount / VELOG_POST_MAX);

            // 5) 가중치 적용
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

            var weights = calculateQueryRepository.findWeightsByResume(resumeId);

            // Source diversity 보정
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

            // 6) 일치/불일치(이번 실행 기준)
            Set<String> observedUnion = new LinkedHashSet<>();
            observedUnion.addAll(ghKeywords); observedUnion.addAll(ghTech);
            observedUnion.addAll(notionKeywords); observedUnion.addAll(velogKeywords);

            Set<String> match = new LinkedHashSet<>(templateKeywords); match.retainAll(observedUnion);
            Set<String> mismatch = new LinkedHashSet<>(templateKeywords); mismatch.removeAll(observedUnion);

            // 7) 결과 upsert
            Resume resumeRef = em.getReference(Resume.class, resumeId);
            Optional<ValidationResult> opt = validationResultRepository.findByResumeId(resumeId);

            ValidationResult result;
            if (opt.isEmpty()) {
                ValidationIssue issue = validationIssueRepository.save(
                        ValidationIssue.builder()
                                .validationResult(ValidationIssue.ValidationResult.SUCCESS)
                                .build()
                );
                result = validationResultRepository.save(
                        ValidationResult.builder()
                                .resume(resumeRef)
                                .validationIssue(issue)
                                .adjustedTotal(adjustedTotal)
                                .build()
                );
            } else {
                result = opt.get();
                validationResultRepository.updateAdjustedTotal(result.getId(), adjustedTotal);
            }

            // 8) 리포트 JSON 생성
            String reportJson = buildReportJson(
                    templateKeywords, ghKeywords, ghTech, notionKeywords, velogKeywords,
                    githubRepoCount, githubCommitCount, velogPostCount, velogDateCount,
                    githubRepoScore, githubCommitScore, githubKeywordMatch, githubTopicMatch,
                    notionKeywordMatch, velogKeywordMatch, velogPostScore, velogDateScore,
                    certScore, sourceDiversityFactor, adjustedTotal, weights
            );

            // 9) 카테고리별 로그 9건 적재
            List<ValidationResultLog> logs = new ArrayList<>();

            logs.add(ValidationResultLog.builder()
                    .validationResult(result)
                    .logType(ValidationResultLog.ValidationLogType.GITHUB_REPO_COUNT)
                    .validationScore(githubRepoScore)
                    .keywordList(reportJson)
                    .matchFields("")
                    .mismatchFields("[]")
                    .validatedAt(LocalDateTime.now())
                    .build());

            logs.add(ValidationResultLog.builder()
                    .validationResult(result)
                    .logType(ValidationResultLog.ValidationLogType.GITHUB_COMMIT_COUNT)
                    .validationScore(githubCommitScore)
                    .keywordList(reportJson)
                    .matchFields("")
                    .mismatchFields("[]")
                    .validatedAt(LocalDateTime.now())
                    .build());

            {
                MM p = mm.apply(templateKeywords, ghKeywords);
                logs.add(ValidationResultLog.builder()
                        .validationResult(result)
                        .logType(ValidationResultLog.ValidationLogType.GITHUB_KEYWORD_MATCH)
                        .validationScore(githubKeywordMatch)
                        .keywordList(reportJson)
                        .matchFields(p.matchCsv())
                        .mismatchFields(p.mismatchJson())
                        .validatedAt(LocalDateTime.now())
                        .build());
            }
            {
                MM p = mm.apply(templateKeywords, ghTech);
                logs.add(ValidationResultLog.builder()
                        .validationResult(result)
                        .logType(ValidationResultLog.ValidationLogType.GITHUB_TOPIC_MATCH)
                        .validationScore(githubTopicMatch)
                        .keywordList(reportJson)
                        .matchFields(p.matchCsv())
                        .mismatchFields(p.mismatchJson())
                        .validatedAt(LocalDateTime.now())
                        .build());
            }
            {
                MM p = mm.apply(templateKeywords, notionKeywords);
                logs.add(ValidationResultLog.builder()
                        .validationResult(result)
                        .logType(ValidationResultLog.ValidationLogType.NOTION_KEYWORD_MATCH)
                        .validationScore(notionKeywordMatch)
                        .keywordList(reportJson)
                        .matchFields(p.matchCsv())
                        .mismatchFields(p.mismatchJson())
                        .validatedAt(LocalDateTime.now())
                        .build());
            }
            {
                MM p = mm.apply(templateKeywords, velogKeywords);
                logs.add(ValidationResultLog.builder()
                        .validationResult(result)
                        .logType(ValidationResultLog.ValidationLogType.VELOG_KEYWORD_MATCH)
                        .validationScore(velogKeywordMatch)
                        .keywordList(reportJson)
                        .matchFields(p.matchCsv())
                        .mismatchFields(p.mismatchJson())
                        .validatedAt(LocalDateTime.now())
                        .build());
            }

            logs.add(ValidationResultLog.builder()
                    .validationResult(result)
                    .logType(ValidationResultLog.ValidationLogType.VELOG_POST_COUNT)
                    .validationScore(velogPostScore)
                    .keywordList(reportJson)
                    .matchFields("")
                    .mismatchFields("[]")
                    .validatedAt(LocalDateTime.now())
                    .build());

            logs.add(ValidationResultLog.builder()
                    .validationResult(result)
                    .logType(ValidationResultLog.ValidationLogType.VELOG_RECENT_ACTIVITY)
                    .validationScore(velogDateScore)
                    .keywordList(reportJson)
                    .matchFields("")
                    .mismatchFields("[]")
                    .validatedAt(LocalDateTime.now())
                    .build());

            logs.add(ValidationResultLog.builder()
                    .validationResult(result)
                    .logType(ValidationResultLog.ValidationLogType.CERTIFICATE_MATCH)
                    .validationScore(certScore)
                    .keywordList(reportJson)
                    .matchFields("")
                    .mismatchFields("[]")
                    .validatedAt(LocalDateTime.now())
                    .build());

            validationResultLogRepository.saveAll(logs);

            // 10) 이력서 상태 VALIDATED 전환
            int updated = resumeRepository.updateStatusValidation(resumeId, Resume.ResumeStatus.VALIDATED);
            if (updated != 1) {
                log.error("Resume status update affected {} rows (expected 1). resumeId={}", updated, resumeId);
            }

            // 11) 로그 집계로 결과 match/mismatch 키워드 갱신
            aggregateAndUpdateResultKeywords(result.getId());

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

        ValidationResult target = validationResultRepository.findLatestByResume(resumeId)
                .orElseThrow(() -> new CustomException(ValidationErrorCode.RESUME_NOT_FOUND));
        if (target.getAdjustedTotal() == null) throw new CustomException(ValidationErrorCode.INVALID_REQUEST);

        double x = target.getAdjustedTotal();
        int n = population.size(), less = 0, equal = 0;
        for (ValidationResult vr : population) {
            double v = Optional.ofNullable(vr.getAdjustedTotal()).orElse(0.0);
            if (v < x) less++;
            else if (Double.compare(v, x) == 0) equal++;
        }
        double percentile = (less + 0.5 * equal) / n;
        double finalScore = Math.max(0.0, Math.min(1.0, percentile)) * 100.0;

        validationResultRepository.updateFinalScoreAndResultAt(target.getId(), finalScore, LocalDateTime.now());
        aggregateAndUpdateResultKeywords(target.getId());
        return target.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public ValidationFinalSummaryResponseDto getFinalSummary(ClientUser clientUser, UUID resumeId) {
        validateReadRole(clientUser.getRole());
        ValidationResult vr = validationResultRepository.findLatestByResume(resumeId)
                .orElseThrow(() -> new CustomException(ValidationErrorCode.RESUME_NOT_FOUND));
        return ValidationFinalSummaryResponseDto.builder()
                .resultId(vr.getId())
                .resumeId(resumeId)
                .finalScore(vr.getFinalScore())
                .matchKeyword(vr.getMatchKeyword())
                .mismatchKeyword(vr.getMismatchKeyword())
                .resultAt(vr.getResultAt())
                .descriptionComment(vr.getDescriptionComment())
                .build();
    }

    @Override
    @Transactional
    public void updateResultComment(ClientUser clientUser, UUID resultId, String comment) {
        validateWriteRole(clientUser.getRole());
        int u = validationResultRepository.updateDescriptionComment(resultId, comment);
        if (u == 0) throw new CustomException(ValidationErrorCode.RESUME_NOT_FOUND);
    }

    // ===== 내부 유틸 =====

    private void aggregateAndUpdateResultKeywords(UUID resultId) {
        List<ValidationResultLog> logs = validationResultLogRepository.findAllByResultId(resultId);

        Map<String, Integer> freq = new HashMap<>();
        Set<String> mismatchUnion = new LinkedHashSet<>();

        for (ValidationResultLog l : logs) {
            Set<String> matches = KeywordUtils.splitCsvToSet(l.getMatchFields());
            for (String m : matches) freq.merge(m, 1, Integer::sum);

            mismatchUnion.addAll(KeywordUtils.parseKeywords(l.getMismatchFields())); // JSON배열 파싱
        }

        String matchTop5 = freq.entrySet().stream()
                .sorted((a,b) -> {
                    int by = Integer.compare(b.getValue(), a.getValue());
                    return by != 0 ? by : a.getKey().compareTo(b.getKey());
                })
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.joining(", "));

        List<String> mis = new ArrayList<>(mismatchUnion);
        Collections.shuffle(mis);
        String mismatchRand5 = mis.stream().limit(5).collect(Collectors.joining(", "));

        validationResultRepository.updateAggregatedKeywords(resultId, emptyToNull(matchTop5), emptyToNull(mismatchRand5));
    }

    private UUID saveIssueAndLogsOnError(UUID resumeId, Exception ex) {
        ValidationIssue issue = validationIssueRepository.save(
                ValidationIssue.builder()
                        .validationResult(ValidationIssue.ValidationResult.FAILED)
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
                        .mismatchFields("[]")
                        .matchFields("")
                        .validatedAt(LocalDateTime.now())
                        .build()
        );
        return result.getId();
    }

    private static String emptyToNull(String s) { return (s == null || s.isBlank()) ? null : s; }
    private static double clamp01(double v) { return (Double.isNaN(v) || Double.isInfinite(v)) ? 0.0 : Math.max(0.0, Math.min(1.0, v)); }

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
        set.stream().filter(Objects::nonNull).forEach(arr::add);
        return arr;
    }
    private static String truncate(String s, int max) { return (s == null || s.length() <= max) ? s : s.substring(0, max); }
    private static String escapeJson(String s) { return (s == null) ? null : s.replace("\"", "\\\""); }
}
