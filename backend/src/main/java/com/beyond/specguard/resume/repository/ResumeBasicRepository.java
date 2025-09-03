package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.ResumeBasic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ResumeBasicRepository extends JpaRepository<ResumeBasic, UUID> {
    Optional<ResumeBasic> findByResume_Id(UUID resumeId);
}
