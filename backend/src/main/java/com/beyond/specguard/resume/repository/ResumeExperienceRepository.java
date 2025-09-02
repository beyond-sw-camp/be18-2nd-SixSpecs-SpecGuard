package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.ResumeExperience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeExperienceRepository extends JpaRepository<ResumeExperience, String> {
    List<ResumeExperience> findByResumeId(String resumeId);
}
