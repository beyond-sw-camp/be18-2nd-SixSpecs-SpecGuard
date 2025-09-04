package com.beyond.specguard.resume.dto.request;

public record ResumeUpdateRequest(

        String templateId,
        String name,
        String phone,
        String email

) {
}
