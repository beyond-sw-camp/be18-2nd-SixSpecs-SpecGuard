package com.beyond.specguard.resume.model.repository;

import com.beyond.specguard.resume.model.entity.core.ResumeBasic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ResumeBasicRepository extends JpaRepository<ResumeBasic, UUID> {
    Optional<ResumeBasic> findByResume_Id(UUID resumeId);
    void deleteByResume_Id(UUID resumeId);
    boolean existsByResume_Id(UUID resumeId);
}
