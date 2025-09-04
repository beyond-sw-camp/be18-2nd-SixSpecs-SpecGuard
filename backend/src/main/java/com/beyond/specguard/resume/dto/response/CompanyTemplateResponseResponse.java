package com.beyond.specguard.resume.dto.response;

public record CompanyTemplateResponseResponse(
        String id,
        String resumeId,
        String fieldId,
        String answer,
        String createdAt,
        String updatedAt
) {
}
