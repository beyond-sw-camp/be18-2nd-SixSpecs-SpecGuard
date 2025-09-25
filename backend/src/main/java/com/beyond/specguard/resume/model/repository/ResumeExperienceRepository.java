package com.beyond.specguard.resume.model.repository;

import com.beyond.specguard.resume.model.entity.ResumeExperience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResumeExperienceRepository extends JpaRepository<ResumeExperience, UUID> {
    void deleteByResume_Id(UUID resumeId);
}