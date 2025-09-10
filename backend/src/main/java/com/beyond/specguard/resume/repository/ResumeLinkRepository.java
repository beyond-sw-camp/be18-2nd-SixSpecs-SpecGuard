package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.ResumeLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResumeLinkRepository extends JpaRepository<ResumeLink, UUID> {
    void deleteByResume_Id(UUID resumeId);

}
