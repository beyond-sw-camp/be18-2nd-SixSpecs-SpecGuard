package com.beyond.specguard.companytemplate.model.service;

import com.beyond.specguard.companytemplate.model.dto.command.SearchTemplateCommand;
import com.beyond.specguard.companytemplate.model.dto.command.UpdateTemplateBasicCommand;
import com.beyond.specguard.companytemplate.model.dto.command.UpdateTemplateDetailCommand;
import com.beyond.specguard.companytemplate.model.dto.request.CompanyTemplateBasicRequestDto;
import com.beyond.specguard.companytemplate.model.dto.request.CompanyTemplateDetailRequestDto;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface CompanyTemplateService {
    CompanyTemplate getCompanyTemplate(UUID templateId);

    void deleteTemplate(UUID templateId);

    CompanyTemplate updateBasic(UpdateTemplateBasicCommand command);

    CompanyTemplate updateDetail(UpdateTemplateDetailCommand command);

    CompanyTemplate createDetailTemplate(CompanyTemplateDetailRequestDto requestDto);

    CompanyTemplate createBasicTemplate(CompanyTemplateBasicRequestDto basicRequestDto);

    Page<CompanyTemplate> getTemplates(SearchTemplateCommand templateCommand);
}
