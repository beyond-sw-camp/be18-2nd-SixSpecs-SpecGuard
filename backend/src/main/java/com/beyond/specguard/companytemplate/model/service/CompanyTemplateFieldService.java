package com.beyond.specguard.companytemplate.model.service;

import com.beyond.specguard.companytemplate.model.entity.CompanyTemplateField;

import java.util.List;
import java.util.UUID;

public interface CompanyTemplateFieldService {
    List<CompanyTemplateField> createField(List<CompanyTemplateField> companyTemplateFields);
    List<CompanyTemplateField> getFields(UUID templateId);
}
