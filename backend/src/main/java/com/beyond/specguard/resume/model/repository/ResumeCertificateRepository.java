package com.beyond.specguard.resume.model.repository;

import com.beyond.specguard.resume.model.entity.ResumeCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ResumeCertificateRepository extends JpaRepository<ResumeCertificate, UUID> {
    Optional<ResumeCertificate> findByIdAndResume_Id(UUID id, UUID resumeId);
    void deleteByResume_Id(UUID resumeId);

}
