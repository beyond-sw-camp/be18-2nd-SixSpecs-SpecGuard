package com.beyond.specguard.resume.dto.resume.response;

import com.beyond.specguard.resume.entity.common.enums.ResumeStatus;

import java.time.LocalDateTime;

public record ResumeResponse(
        String id,
        String templateId,
        ResumeStatus status,
        String name,
        String phone,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
