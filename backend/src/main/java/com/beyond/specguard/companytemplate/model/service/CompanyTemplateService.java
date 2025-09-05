package com.beyond.specguard.companytemplate.model.service;

import com.beyond.specguard.companytemplate.model.dto.command.UpdateTemplateDetailCommand;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;

import java.util.UUID;

public interface CompanyTemplateService {
    CompanyTemplate createTemplate(CompanyTemplate template);

    CompanyTemplate getCompanyTemplate(UUID templateId);

    void deleteTemplate(UUID templateId);

    CompanyTemplate updateTemplate(CompanyTemplate companyTemplate);

    CompanyTemplate updateDetail(UpdateTemplateDetailCommand command);
}
