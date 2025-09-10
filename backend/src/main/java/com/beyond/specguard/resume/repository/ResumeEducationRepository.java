package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.ResumeEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ResumeEducationRepository extends JpaRepository<ResumeEducation, UUID> {
    void deleteByResume_Id(UUID resumeId);
}