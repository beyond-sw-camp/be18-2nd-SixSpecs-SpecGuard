package com.beyond.specguard.resume.dto.resume.request;

import com.beyond.specguard.resume.entity.common.enums.ResumeStatus;

public record ResumeStatusUpdateRequest(
        ResumeStatus status
) {
}
