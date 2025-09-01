package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.common.enums.ResumeStatus;
import com.beyond.specguard.resume.entity.core.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, String> {
}
