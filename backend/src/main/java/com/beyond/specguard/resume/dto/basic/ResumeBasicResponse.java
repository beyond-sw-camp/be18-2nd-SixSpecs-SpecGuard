package com.beyond.specguard.resume.dto.basic;

import com.beyond.specguard.resume.entity.common.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;

//API 응답으로 내려줄 dto
public record ResumeBasicResponse(
        String id,
        String resumeId,
        String englishName,
        Gender gender,
        LocalDate birthDate,
        String nationality,
        String address,
        String specialty,
        String hobbies,
        String applyField,
        String profileImageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
