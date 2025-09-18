package com.beyond.specguard.companytemplate.model.repository;

import com.beyond.specguard.companytemplate.model.entity.CompanyTemplateField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CompanyTemplateFieldRepository extends JpaRepository<CompanyTemplateField, UUID> {
    List<CompanyTemplateField> findAllByTemplate_Id(UUID templateId);

    void deleteAllByTemplate_Id(UUID templateId);

    UUID id(UUID id);
}
