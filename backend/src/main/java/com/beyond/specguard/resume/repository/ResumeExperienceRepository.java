package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.ResumeExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ResumeExperienceRepository extends JpaRepository<ResumeExperience, UUID> {

    @Query("""
        select (count(x) > 0) from ResumeExperience x
        where x.resume.id = :resumeId
          and (:endDate is null or x.startDate <= :endDate)
          and (x.endDate is null or x.endDate >= :startDate)
    """)
    boolean existsPeriodOverlap(UUID resumeId, LocalDate startDate, LocalDate endDate);

    List<ResumeExperience> findByResume_Id(UUID resumeId);
    void deleteByResume_Id(UUID resumeId);
    long countByResume_Id(UUID resumeId);
}