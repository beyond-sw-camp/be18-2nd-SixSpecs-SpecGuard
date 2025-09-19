package com.beyond.specguard.validation.model.service;

import com.beyond.specguard.company.common.model.entity.ClientUser;
import com.beyond.specguard.validation.model.dto.request.ValidationCalculateRequestDto;
import com.beyond.specguard.validation.model.dto.request.ValidationPercentileRequestDto;

import java.util.UUID;

public interface ValidationResultService {
    UUID calculateAndSave(ClientUser clientUser, ValidationCalculateRequestDto request);
    UUID calculatePercentile(ClientUser clientUser, ValidationPercentileRequestDto request);
}
