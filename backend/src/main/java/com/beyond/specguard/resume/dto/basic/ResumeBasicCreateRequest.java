package com.beyond.specguard.resume.dto.basic;

import com.beyond.specguard.resume.entity.common.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
