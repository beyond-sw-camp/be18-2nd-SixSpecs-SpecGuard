package com.beyond.specguard.companytemplate.model.service;

import com.beyond.specguard.companytemplate.model.dto.command.CreateCompanyTemplateFieldCommand;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplateField;

import java.util.List;
import java.util.UUID;

public interface CompanyTemplateFieldService {
    List<CompanyTemplateField> getFields(UUID templateId);

    void deleteFields(UUID templateId);

    List<CompanyTemplateField> updateFields(List<CompanyTemplateField> companyTemplateFields);

    void deleteFieldById(UUID id);

    List<CompanyTemplateField> createFields(CreateCompanyTemplateFieldCommand fields);
}
