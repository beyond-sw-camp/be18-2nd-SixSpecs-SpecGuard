package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.ResumeEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ResumeEducationRepository extends JpaRepository<ResumeEducation, UUID> {
    @Query("""
SELECT CASE WHEN COUNT(e)>0 THEN true ELSE false END
FROM ResumeEducation e
WHERE e.resume.id = :resumeId
AND (e.startDate <= COALESCE(:endDate, DATE '9999-12-31')
AND COALESCE(e.endDate, DATE '9999-12-31') >= :startDate)
""")
    boolean existsPeriodOverlap(UUID resumeId, LocalDate startDate, LocalDate endDate);
    List<ResumeEducation> findByResume_Id(UUID resumeId);
    void deleteByResume_Id(UUID resumeId);
    long countByResume_Id(UUID resumeId);
}
