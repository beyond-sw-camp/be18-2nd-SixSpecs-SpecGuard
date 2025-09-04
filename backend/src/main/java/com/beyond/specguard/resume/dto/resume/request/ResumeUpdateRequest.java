package com.beyond.specguard.resume.dto.resume.request;

import com.beyond.specguard.resume.entity.common.enums.ResumeStatus;

import java.util.UUID;

public record ResumeUpdateRequest(

        ResumeStatus status,
        String name,
        String phone,
        String email,
        UUID templateId,
        String passwordHash
) {
}
