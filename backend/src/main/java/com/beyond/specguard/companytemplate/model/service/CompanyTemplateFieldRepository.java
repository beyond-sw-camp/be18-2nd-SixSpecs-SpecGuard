package com.beyond.specguard.companytemplate.model.service;

import com.beyond.specguard.companytemplate.model.entity.CompanyTemplateField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface CompanyTemplateFieldRepository extends JpaRepository<CompanyTemplateField, UUID> {
}
