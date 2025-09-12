package com.beyond.specguard.tempresume;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ResumeRepository extends JpaRepository<Resume, UUID> {
    Optional<Resume> findByEmail(String string);

    Optional<Resume> findByEmailAndTemplateId(String email, UUID templateId);
}
