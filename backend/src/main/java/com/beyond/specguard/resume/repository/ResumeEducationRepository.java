package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.ResumeEducation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResumeEducationRepository extends JpaRepository<ResumeEducation, UUID> {
    void deleteByResume_Id(UUID resumeId);
}