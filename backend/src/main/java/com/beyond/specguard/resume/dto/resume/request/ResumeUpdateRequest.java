package com.beyond.specguard.resume.dto.resume.request;

import com.beyond.specguard.resume.entity.common.enums.ResumeStatus;

public record ResumeUpdateRequest(

        ResumeStatus status,
        String name,
        String phone,
        String email,
        String templateId,
        String passwordHash
) {
}
