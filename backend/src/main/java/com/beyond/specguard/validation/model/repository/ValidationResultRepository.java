package com.beyond.specguard.validation.model.repository;


import com.beyond.specguard.validation.model.entity.ValidationResult;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ValidationResultRepository extends JpaRepository<ValidationResult, UUID> {
    @Query("""
    select vr
    from ValidationResult vr
    join vr.resume r
    join r.template t
    where t.id = :templateId
    and r.status =  com.beyond.specguard.resume.model.entity.Resume.ResumeStatus.PROCESSING
    and vr.adjustedTotal is not null
            """)
    List<ValidationResult> findAllValidatedByTemplateId(@Param("templateId") UUID templateId);

    // Resume 단건에 연결된 ValidationResult 조회
    @Query("""
        select vr
          from ValidationResult vr
          join vr.resume r
         where r.id = :resumeId
    """)
    Optional<ValidationResult> findByResumeId(@Param("resumeId") UUID resumeId);

    // 특정 ResumeId에 대한 ValidationResult 로그들 (최신순)
    @Query("""
        select vr
          from ValidationResult vr
          join vr.resume r
         where r.id = :resumeId
         order by vr.createdAt desc
    """)
    List<ValidationResult> findAllByResumeOrderByCreatedAtDesc(@Param("resumeId") UUID resumeId);

    // 최신 로그만 단건으로 가져오기
    default Optional<ValidationResult> findLatestByResume(UUID resumeId) {
        List<ValidationResult> list = findAllByResumeOrderByCreatedAtDesc(resumeId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    // ===== Update Queries =====

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ValidationResult vr set vr.finalScore = :finalScore, vr.resultAt = :resultAt where vr.id = :resultId")
    int updateFinalScoreAndResultAt(@Param("resultId") UUID resultId,
                                    @Param("finalScore") Double finalScore,
                                    @Param("resultAt") LocalDateTime resultAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ValidationResult vr set vr.descriptionComment = :comment where vr.id = :resultId")
    int updateDescriptionComment(@Param("resultId") UUID resultId, @Param("comment") String comment);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ValidationResult vr set vr.matchKeyword = :matchKw, vr.mismatchKeyword = :mismatchKw where vr.id = :resultId")
    int updateAggregatedKeywords(@Param("resultId") UUID resultId,
                                 @Param("matchKw") String matchKw,
                                 @Param("mismatchKw") String mismatchKw);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ValidationResult vr set vr.adjustedTotal = :adjustedTotal where vr.id = :resultId")
    int updateAdjustedTotal(@Param("resultId") UUID resultId,
                            @Param("adjustedTotal") Double adjustedTotal);
}