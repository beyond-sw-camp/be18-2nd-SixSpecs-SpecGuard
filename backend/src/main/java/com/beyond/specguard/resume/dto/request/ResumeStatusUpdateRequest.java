package com.beyond.specguard.resume.dto.request;

import com.beyond.specguard.resume.entity.common.enums.ResumeStatus;
import jakarta.validation.constraints.NotNull;

public record ResumeStatusUpdateRequest(
        @NotNull
        ResumeStatus status
) {
}
