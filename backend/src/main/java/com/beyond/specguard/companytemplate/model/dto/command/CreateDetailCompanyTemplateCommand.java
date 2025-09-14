package com.beyond.specguard.companytemplate.model.dto.command;

import com.beyond.specguard.company.common.model.entity.ClientUser;
import com.beyond.specguard.companytemplate.model.dto.request.CompanyTemplateDetailRequestDto;

public record CreateDetailCompanyTemplateCommand(
        ClientUser clientUser,
        CompanyTemplateDetailRequestDto requestDto
) {}
