package com.beyond.specguard.companytemplate.model.dto.command;

import com.beyond.specguard.company.common.model.entity.ClientUser;
import com.beyond.specguard.companytemplate.model.dto.request.CompanyTemplateDetailRequestDto;

import java.util.UUID;

public record UpdateTemplateDetailCommand(UUID templateId,
                                          CompanyTemplateDetailRequestDto requestDto,
                                          ClientUser clientUser) {
}
