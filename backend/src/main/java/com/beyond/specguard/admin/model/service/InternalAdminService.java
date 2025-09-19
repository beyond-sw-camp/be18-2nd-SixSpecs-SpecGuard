package com.beyond.specguard.admin.model.service;

import com.beyond.specguard.admin.model.dto.request.InternalAdminRequestDto;
import com.beyond.specguard.admin.model.dto.response.InternalAdminResponseDto;

public interface InternalAdminService {
    InternalAdminResponseDto createAdmin(InternalAdminRequestDto request);
}
