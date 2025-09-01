package com.beyond.specguard.resume.dto.basic;

import com.beyond.specguard.resume.entity.common.enums.Gender;

import java.time.LocalDate;

//수정 요청, Null ok
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
