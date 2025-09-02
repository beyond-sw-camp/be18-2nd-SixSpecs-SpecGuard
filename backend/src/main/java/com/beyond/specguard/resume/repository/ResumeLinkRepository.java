package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.ResumeLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeLinkRepository extends JpaRepository<ResumeLink, String> {

    List<ResumeLink> findAllByResumeId(String resumeId);
    Optional<ResumeLink> findByIdAndResumeId(String id, String resumeId);
    boolean existsByIdAndResumeId(String id, String resumeId);
    void deleteByIdAndResumeId(String id, String resumeId);

}
