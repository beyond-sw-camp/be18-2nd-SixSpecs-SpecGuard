package com.beyond.specguard.companytemplate.model.service;

import com.beyond.specguard.company.common.model.entity.ClientUser;
import com.beyond.specguard.companytemplate.model.dto.command.CreateBasicCompanyTemplateCommand;
import com.beyond.specguard.companytemplate.model.dto.command.CreateDetailCompanyTemplateCommand;
import com.beyond.specguard.companytemplate.model.dto.command.SearchTemplateCommand;
import com.beyond.specguard.companytemplate.model.dto.command.UpdateTemplateBasicCommand;
import com.beyond.specguard.companytemplate.model.dto.command.UpdateTemplateDetailCommand;
import com.beyond.specguard.companytemplate.model.dto.response.CompanyTemplateListResponseDto;
import com.beyond.specguard.companytemplate.model.dto.response.CompanyTemplateResponseDto;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;

import java.util.UUID;

public interface CompanyTemplateService {
    CompanyTemplate getCompanyTemplate(ClientUser clientUser, UUID templateId);

    void deleteTemplate(UUID templateId, ClientUser clientUser);

    CompanyTemplateResponseDto.BasicDto updateBasic(UpdateTemplateBasicCommand command);

    CompanyTemplateResponseDto.DetailDto updateDetail(UpdateTemplateDetailCommand command);

    CompanyTemplateResponseDto.DetailDto createDetailTemplate(CreateDetailCompanyTemplateCommand requestDto);

    CompanyTemplateResponseDto.BasicDto createBasicTemplate(CreateBasicCompanyTemplateCommand basicRequestDto);

    CompanyTemplateListResponseDto getTemplates(SearchTemplateCommand templateCommand);
}
