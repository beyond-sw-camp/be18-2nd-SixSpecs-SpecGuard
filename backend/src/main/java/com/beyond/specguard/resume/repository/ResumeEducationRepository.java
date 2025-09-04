package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.ResumeEducation;
import org.springdoc.core.converters.models.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ResumeEducationRepository extends JpaRepository<ResumeEducation, UUID> {
    List<ResumeEducation> findByResumeId(UUID resumeId);
    List<ResumeEducation> findAllByResumeId(UUID resumeId, Sort sort);

    void deleteByResumeId(UUID resumeId);

    long countByResumeId(UUID resumeId);
}
