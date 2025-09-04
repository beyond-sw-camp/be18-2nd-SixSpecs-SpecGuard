package com.beyond.specguard.resume.dto.request;

import com.beyond.specguard.resume.entity.common.enums.ResumeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ResumeStatusUpdateRequest(
        @Schema(description = "이력서 상태", example = "SUBMITTED")
        @NotNull
        ResumeStatus status
) {
}
