package com.beyond.specguard.admin.model.service;

import com.beyond.specguard.admin.model.dto.InternalAdminRequestDto;
import com.beyond.specguard.admin.model.entity.InternalAdmin;

public interface InternalAdminService {
    InternalAdmin createAdmin(InternalAdminRequestDto request);
}
