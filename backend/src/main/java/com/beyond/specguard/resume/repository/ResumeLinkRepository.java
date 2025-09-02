package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.ResumeLink;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeLinkRepository extends JpaRepository<ResumeLink, String> {
}
