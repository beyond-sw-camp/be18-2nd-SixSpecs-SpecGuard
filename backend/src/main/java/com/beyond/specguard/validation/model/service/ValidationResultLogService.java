package com.beyond.specguard.validation.model.service;

import com.beyond.specguard.company.common.model.entity.ClientUser;
import com.beyond.specguard.validation.model.dto.response.ValidationResultLogResponseDto;
import com.beyond.specguard.validation.model.entity.ValidationResultLog;

import java.util.List;
import java.util.UUID;

public interface ValidationResultLogService {
    List<ValidationResultLogResponseDto> getLogsByResumeId(ClientUser clientUser, UUID resumeId);

}
