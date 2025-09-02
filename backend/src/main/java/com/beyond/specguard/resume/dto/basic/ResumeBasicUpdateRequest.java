package com.beyond.specguard.resume.dto.basic;

import com.beyond.specguard.resume.entity.common.enums.Gender;

import java.time.LocalDate;

public record ResumeBasicUpdateRequest(
        String englishName,
        Gender gender,
        LocalDate birthDate,
        String nationality,
        String applyField,
        String profileImageUrl,
        String address,
        String specialty,
        String hobbies
) {
}
