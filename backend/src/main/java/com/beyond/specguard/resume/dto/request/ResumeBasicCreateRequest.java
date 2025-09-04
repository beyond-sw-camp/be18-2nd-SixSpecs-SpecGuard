package com.beyond.specguard.resume.dto.request;

import com.beyond.specguard.resume.entity.common.enums.Gender;

import java.time.LocalDate;

//생성
public record ResumeBasicCreateRequest(
        String resumeId,
        String englishName,
        Gender gender,
        LocalDate birthDate,
        String nationality,
        String address,
        String specialty,
        String hobbies,
        String applyField,
        String profileImageUrl
) {
}
