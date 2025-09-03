package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.ResumeExperience;
import org.springdoc.core.converters.models.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ResumeExperienceRepository extends JpaRepository<ResumeExperience, UUID> {
    List<ResumeExperience> findByResume_Id(UUID resumeId);
}
