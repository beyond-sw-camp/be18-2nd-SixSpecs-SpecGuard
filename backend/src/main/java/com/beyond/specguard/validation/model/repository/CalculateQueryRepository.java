package com.beyond.specguard.validation.model.repository;

import com.beyond.specguard.resume.model.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CalculateQueryRepository extends JpaRepository<Resume, UUID> {

    // 회사 템플릿 응답 분석 키워드(JSON) 목록
    // company_template_response_analysis.keyword 컬럼(JSON: {"keywords":[...]} )
    @Query(value = """
        SELECT cra.keyword
          FROM company_template_response_analysis cra
          JOIN company_template_response ctr ON ctr.id = cra.response_id
         WHERE ctr.resume_id = :resumeId
        """, nativeQuery = true)
    List<String> findTemplateAnalysisKeywordsJson(@Param("resumeId") UUID resumeId);

    // 플랫폼별 포트폴리오 정제 JSON 목록 (최신순)
    // portfolio_result -> crawling_result -> resume_link.link_type
    @Query(value = """
        SELECT pr.processed_contents
          FROM portfolio_result pr
          JOIN crawling_result cr ON pr.crawling_result_id = cr.id
          JOIN resume_link rl      ON cr.resume_link_id = rl.id
         WHERE cr.resume_id = :resumeId
           AND rl.link_type = :linkType
         ORDER BY pr.created_at DESC
        """, nativeQuery = true)
    List<String> findProcessedContentsByPlatform(@Param("resumeId") UUID resumeId,
                                                 @Param("linkType") String linkType);

    // ===== 자격증 검증 집계 (MariaDB: CAST AS BIGINT 사용 금지, Number 프로젝션으로 수신) =====
    interface CertAggRow {
        Number getCompleted();
        Number getFailed();
    }

    @Query(value = """
        SELECT 
          COALESCE(SUM(CASE WHEN UPPER(cv.status) = 'COMPLETED' THEN 1 ELSE 0 END), 0) AS completed,
          COALESCE(SUM(CASE WHEN UPPER(cv.status) = 'FAILED'     THEN 1 ELSE 0 END), 0) AS failed
        FROM certificate_verification cv
        JOIN resume_certificate rc ON rc.id = cv.certificate_id
       WHERE rc.resume_id = :resumeId
        """, nativeQuery = true)
    CertAggRow countCertificateVerificationRow(@Param("resumeId") UUID resumeId);

    default Map<String, Object> countCertificateVerification(UUID resumeId) {
        CertAggRow row = countCertificateVerificationRow(resumeId);
        int completed = (row == null || row.getCompleted() == null) ? 0 : row.getCompleted().intValue();
        int failed    = (row == null || row.getFailed()    == null) ? 0 : row.getFailed().intValue();
        Map<String, Object> m = new HashMap<>();
        m.put("completed", completed);
        m.put("failed", failed);
        return m;
    }

    // ===== 가중치 조회 (회사별 활성 프로필 기준) =====
    interface WeightRow {
        String getWeightType();
        Double getWeightValue();
    }

    @Query(value = """
        SELECT ew.weight_type AS weightType, ew.weight_value AS weightValue
          FROM resume r
          JOIN company_template   ct ON ct.id = r.template_id
          JOIN evaluation_profile ep ON ep.id = (
                 SELECT epp.id
                   FROM evaluation_profile epp
                  WHERE epp.company_id = ct.company_id
                    AND epp.is_active = 1
                  ORDER BY epp.updated_at DESC
                  LIMIT 1
          )
          JOIN evaluation_weight  ew ON ew.evaluation_profile_id = ep.id
         WHERE r.id = :resumeId
        """, nativeQuery = true)
    List<WeightRow> findWeightsByResume(@Param("resumeId") UUID resumeId);
}
