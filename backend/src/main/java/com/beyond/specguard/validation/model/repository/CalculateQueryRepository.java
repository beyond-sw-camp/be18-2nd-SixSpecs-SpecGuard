package com.beyond.specguard.validation.model.repository;

import com.beyond.specguard.resume.model.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;



public interface CalculateQueryRepository extends JpaRepository<Resume, UUID> {



    // 회사 템플릿 응답 분석 키워드(JSON) 목록
    // company_template_response_analysis.keyword 컬럼(JSON: {"keywords":[...]})
    @Query(value = """
        SELECT cra.keyword
          FROM company_template_response_analysis cra
          JOIN company_template_response ctr ON ctr.id = cra.response_id
         WHERE ctr.resume_id = :resumeId
        """, nativeQuery = true)
    List<String> findTemplateAnalysisKeywordsJson(@Param("resumeId") String resumeId);

    // 플랫폼별 포트폴리오 정제 JSON 목록 (최신순)
    // portfolio_result -> crawling_result -> resume_link.link_type
    @Query(value = """
        SELECT pr.processed_contents
          FROM portfolio_result pr
          JOIN crawling_result cr ON pr.crawling_result_id = cr.id
          JOIN resume_link rl ON cr.resume_link_id = rl.id
         WHERE cr.resume_id = :resumeId
           AND rl.link_type = :linkType
         ORDER BY pr.created_at DESC
        """, nativeQuery = true)
    List<String> findProcessedContentsByPlatform(@Param("resumeId") String resumeId,
                                                 @Param("linkType") String linkType);


    // GitHub 메타데이터 합계(레포/커밋) - resume의 GITHUB 링크 전체 기준
    @Query(value = """
        SELECT COALESCE(SUM(gm.repo_count),0) AS repoSum,
               COALESCE(SUM(gm.commits),0)    AS commitSum
          FROM github_metatdata gm
          JOIN resume_link rl ON gm.resume_link_id = rl.id
         WHERE rl.resume_id = :resumeId
           AND rl.link_type = 'GITHUB'
        """, nativeQuery = true)
    Object[] sumGithubStatsRaw(@Param("resumeId") String resumeId);

    // 자격증 검증 집계
    @Query(value = """
        SELECT 
          COALESCE(SUM(CASE WHEN UPPER(cv.status) IN ('COMPLETED','SUCCESS') THEN 1 ELSE 0 END),0) AS completed,
          COALESCE(SUM(CASE WHEN UPPER(cv.status) = 'FAILED' THEN 1 ELSE 0 END),0)                AS failed
        FROM certificate_verification cv
        JOIN resume_certificate rc ON rc.id = cv.certificate_id
       WHERE rc.resume_id = :resumeId
        """, nativeQuery = true)
    Object[] countCertificateVerificationRaw(@Param("resumeId") String resumeId);

    // 가중치 조회: resume → company_template → evaluation_profile → evaluation_weight
    interface WeightRow {
        String getWeightType();
        Double getWeightValue();
    }
    @Query(value = """
        SELECT ew.weight_type AS weightType, ew.weight_value AS weightValue
          FROM resume r
          JOIN company_template ct ON ct.id = r.template_id
          JOIN evaluation_profile ep ON ep.id = ct.evaluation_profile_id
          JOIN evaluation_weight ew ON ew.evaluation_profile_id = ep.id
         WHERE r.id = :resumeId
        """, nativeQuery = true)
    List<WeightRow> findWeightsByResume(@Param("resumeId") String resumeId);


    default List<String> findTemplateAnalysisKeywordsJson(UUID resumeId) {
        return findTemplateAnalysisKeywordsJsonRaw(resumeId.toString());
    }
    default List<String> findProcessedContentsByPlatform(UUID resumeId, String linkType) {
        return findProcessedContentsByPlatformRaw(resumeId.toString(), linkType);
    }
    default Map<String, Object> sumGithubStats(UUID resumeId) {
        Object[] row = sumGithubStatsRaw(resumeId.toString());
        Map<String, Object> m = new HashMap<>();
        m.put("repoSum",   ((Number) row[0]).intValue());
        m.put("commitSum", ((Number) row[1]).intValue());
        return m;
    }
    default Map<String, Object> countCertificateVerification(UUID resumeId) {
        Object[] row = countCertificateVerificationRaw(resumeId.toString());
        Map<String, Object> m = new HashMap<>();
        m.put("completed", ((Number) row[0]).intValue());
        m.put("failed",    ((Number) row[1]).intValue());
        return m;
    }
    default List<WeightRow> findWeightsByResume(UUID resumeId) {
        return findWeightsByResumeRaw(resumeId.toString());
    }
}