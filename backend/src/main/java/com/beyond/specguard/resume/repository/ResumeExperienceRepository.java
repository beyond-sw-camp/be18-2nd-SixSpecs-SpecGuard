package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.ResumeExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ResumeExperienceRepository extends JpaRepository<ResumeExperience, UUID> {
    @Query("""
        SELECT CASE WHEN COUNT(x)>0 THEN true ELSE false END
        FROM ResumeExperience x
        WHERE x.resume.id = :resumeId
          AND (x.startDate <= :endDate AND :startDate <= x.endDate)
    """)
    boolean existsPeriodOverlap(UUID resumeId, LocalDate startDate, LocalDate endDate);
    List<ResumeExperience> findByResume_Id(UUID resumeId);
    void deleteByResume_Id(UUID resumeId);
    long countByResume_Id(UUID resumeId);
}
