package com.beyond.specguard.companytemplate.model.dto.command;

import com.beyond.specguard.companytemplate.model.dto.request.TemplateFieldRequestDto;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;

public record CreateCompanyTemplateFieldCommand(CompanyTemplate companyTemplate, TemplateFieldRequestDto templateFieldRequestDto) {
}
