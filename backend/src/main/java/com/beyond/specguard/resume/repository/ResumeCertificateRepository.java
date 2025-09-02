package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.ResumeCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeCertificateRepository extends JpaRepository<ResumeCertificate, String> {
}
