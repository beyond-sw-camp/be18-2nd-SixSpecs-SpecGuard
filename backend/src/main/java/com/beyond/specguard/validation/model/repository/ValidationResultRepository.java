package com.beyond.specguard.validation.model.repository;


import com.beyond.specguard.validation.model.entity.ValidationResult;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ValidationResultRepository extends JpaRepository<ValidationResult, UUID> {
    @Query("""
       select vr
         from ValidationResult vr
         join vr.resume r
        where r.template.id = :templateId
          and r.status = com.beyond.specguard.resume.model.entity.Resume.ResumeStatus.VALIDATED
          and vr.adjustedTotal is not null
    """)
    List<ValidationResult> findAllValidatedByTemplateId(@Param("templateId") UUID templateId);

    @Query("""
       select vr
         from ValidationResult vr
         join vr.resume r
        where r.id = :resumeId
        order by vr.createdAt desc
    """)
    List<ValidationResult> findAllByResumeOrderByCreatedAtDesc(@Param("resumeId") UUID resumeId);

    @Query("""
       select vr
         from ValidationResult vr
         join vr.resume r
        where r.id = :resumeId
        order by vr.createdAt desc
    """)
    default Optional<ValidationResult> findLatestByResume(@Param("resumeId") UUID resumeId) {
        List<ValidationResult> list = findAllByResumeOrderByCreatedAtDesc(resumeId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ValidationResult vr set vr.finalScore = :finalScore, vr.resultAt = :resultAt where vr.id = :resultId")
    int updateFinalScoreAndResultAt(@Param("resultId") UUID resultId,
                                    @Param("finalScore") Double finalScore,
                                    @Param("resultAt") java.time.LocalDateTime resultAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ValidationResult vr set vr.descriptionComment = :comment where vr.id = :resultId")
    int updateDescriptionComment(@Param("resultId") UUID resultId, @Param("comment") String comment);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ValidationResult vr set vr.matchKeyword = :matchKw, vr.mismatchKeyword = :mismatchKw where vr.id = :resultId")
    int updateAggregatedKeywords(@Param("resultId") UUID resultId,
                                 @Param("matchKw") String matchKw,
                                 @Param("mismatchKw") String mismatchKw);
}