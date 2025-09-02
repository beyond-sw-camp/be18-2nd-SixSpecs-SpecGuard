package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.ResumeCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeCertificateRepository extends JpaRepository<ResumeCertificate, String> {
    List<ResumeCertificate> findAllByResumeId(String resumeId);
    Optional<ResumeCertificate> findByIdAndResumeId(String id, String resumeId);
    boolean existsByIdAndResumeId(String id, String resumeId);
    void deleteByIdAndResumeId(String id, String resumeId);
}
