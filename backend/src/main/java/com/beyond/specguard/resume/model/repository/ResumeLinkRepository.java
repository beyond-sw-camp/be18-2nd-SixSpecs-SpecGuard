package com.beyond.specguard.resume.model.repository;

import com.beyond.specguard.resume.model.entity.core.ResumeLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ResumeLinkRepository extends JpaRepository<ResumeLink, UUID> {
    void deleteByResume_Id(UUID resumeId);

}
