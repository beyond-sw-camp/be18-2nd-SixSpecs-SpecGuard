package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.ResumeCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResumeCertificateRepository extends JpaRepository<ResumeCertificate, UUID> {
    Optional<ResumeCertificate> findByIdAndResume_Id(UUID id, UUID resumeId);
    void deleteByResume_Id(UUID resumeId);

}
