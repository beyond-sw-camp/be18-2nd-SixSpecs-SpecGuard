package com.beyond.specguard.validation.model.repository;

import com.beyond.specguard.validation.model.entity.ValidationResult;
import com.beyond.specguard.validation.model.entity.ValidationResultLog;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

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
    """)
    Optional<ValidationResult> findByResumeId(@Param("resumeId") UUID resumeId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ValidationResult vr set vr.finalScore = :finalScore where vr.id = :resultId")
    int updateFinalScore(@Param("resultId") UUID resultId, @Param("finalScore") Double finalScore);
}