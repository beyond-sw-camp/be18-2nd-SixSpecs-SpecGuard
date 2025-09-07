package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.ResumeLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ResumeLinkRepository extends JpaRepository<ResumeLink, UUID> {

    List<ResumeLink> findByResume_Id(UUID resumeId);
    void deleteByResume_Id(UUID resumeId);
    long countByResume_Id(UUID resumeId);

}
