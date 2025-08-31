package com.beyond.specguard.resume.dto.request;

import com.beyond.specguard.resume.entity.common.enums.ResumeStatus;

public record ResumeUpdateRequest(

        String name,
        String phone,
        String email,
        String templateId
) {
}
