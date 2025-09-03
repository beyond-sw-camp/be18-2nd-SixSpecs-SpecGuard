package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.ResumeCertificate;
import org.springdoc.core.converters.models.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResumeCertificateRepository extends JpaRepository<ResumeCertificate, UUID> {
    List<ResumeCertificate> findByResume_Id(UUID resumeId);


}
