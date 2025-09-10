package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.ResumeExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ResumeExperienceRepository extends JpaRepository<ResumeExperience, UUID> {
    void deleteByResume_Id(UUID resumeId);
}