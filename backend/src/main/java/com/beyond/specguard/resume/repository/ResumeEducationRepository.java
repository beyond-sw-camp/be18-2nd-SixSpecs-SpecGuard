package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.ResumeEducation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeEducationRepository extends JpaRepository<ResumeEducation, String> {
    List<ResumeEducation> findByResumeId(String resumeId);
}
