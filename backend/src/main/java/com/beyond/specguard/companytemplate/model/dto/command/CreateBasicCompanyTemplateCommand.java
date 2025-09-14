package com.beyond.specguard.companytemplate.model.dto.command;

import com.beyond.specguard.company.common.model.entity.ClientUser;
import com.beyond.specguard.companytemplate.model.dto.request.CompanyTemplateBasicRequestDto;

public record CreateBasicCompanyTemplateCommand(
        ClientUser clientUser,
        CompanyTemplateBasicRequestDto basicRequestDto
){}
