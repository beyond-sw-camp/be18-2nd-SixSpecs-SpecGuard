package com.beyond.specguard.resume.dto.templateResponse;

public record CompanyTemplateResponseResponse(
        String id,
        String resumeId,
        String fieldId,
        String answer,
        String createdAt,
        String updatedAt
) {
}
