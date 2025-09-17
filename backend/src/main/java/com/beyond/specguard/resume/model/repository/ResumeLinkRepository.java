package com.beyond.specguard.resume.model.repository;

import com.beyond.specguard.resume.model.entity.common.enums.LinkType;
import com.beyond.specguard.resume.model.entity.core.ResumeLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResumeLinkRepository extends JpaRepository<ResumeLink, UUID> {
    void deleteByResume_Id(UUID resumeId);
    List<ResumeLink> findByResume_Id(UUID resumeId);

}
