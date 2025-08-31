package com.beyond.specguard.resume.dto.response;

import com.beyond.specguard.resume.entity.common.enums.ResumeStatus;
import com.beyond.specguard.resume.entity.core.Resume;

import java.time.LocalDateTime;

public record ResumeResponse(
        String id,
        String templateId,
        String name,
        String phone,
        String email,
        ResumeStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
