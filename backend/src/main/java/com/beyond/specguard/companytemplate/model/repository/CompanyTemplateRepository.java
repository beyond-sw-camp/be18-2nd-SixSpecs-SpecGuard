package com.beyond.specguard.companytemplate.model.repository;

import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CompanyTemplateRepository extends JpaRepository<CompanyTemplate, UUID> {
}
