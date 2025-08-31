package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.ResumeBasic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeBasicRepository extends JpaRepository<ResumeBasic, String> {
    Optional<ResumeBasic> findByResumeId(String resumeId);
}
