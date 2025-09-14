package com.beyond.specguard.resume.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ResumeSubmitRequest(
        @Schema(description = "제출 대상 회사 ID (client_company.id)")
        @NotNull
        UUID companyId
) {}
