package com.beyond.specguard.resume.model.dto.response;

import com.beyond.specguard.resume.model.entity.common.enums.ResumeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

public record ResumeSubmitResponse(
        @Schema(description = "제출 이력 ID")
        UUID submissionId,
        UUID resumeId,
        UUID companyId,
        @Schema(description = "제출 시각")
        LocalDateTime submittedAt,
        @Schema(description = "이력서 현재 상태")
        ResumeStatus status
) {}
