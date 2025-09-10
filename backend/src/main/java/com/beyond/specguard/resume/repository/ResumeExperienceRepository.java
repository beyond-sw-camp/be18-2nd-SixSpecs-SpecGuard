package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.ResumeExperience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResumeExperienceRepository extends JpaRepository<ResumeExperience, UUID> {
    void deleteByResume_Id(UUID resumeId);
}