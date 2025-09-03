package com.beyond.specguard.companytemplate.model.service;

import com.beyond.specguard.companytemplate.model.entity.CompanyTemplateField;

import java.util.List;

public interface CompanyTemplateFieldService {
    List<CompanyTemplateField> createField(List<CompanyTemplateField> companyTemplateFieldStream);
}
