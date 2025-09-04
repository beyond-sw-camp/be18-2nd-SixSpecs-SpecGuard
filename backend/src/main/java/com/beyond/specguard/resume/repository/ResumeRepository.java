package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ResumeRepository extends JpaRepository<Resume, UUID> {
    Optional<Resume> findByEmail(String email);
    boolean existsByEmail(String email);
}
