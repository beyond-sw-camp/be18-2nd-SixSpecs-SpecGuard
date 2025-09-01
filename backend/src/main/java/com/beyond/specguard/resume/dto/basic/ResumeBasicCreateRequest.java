package com.beyond.specguard.resume.dto.basic;

import com.beyond.specguard.resume.entity.common.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

//생성
public record ResumeBasicCreateRequest(
        @NotBlank
        String resumeId,

        @NotBlank
        String englishName,

        @NotBlank
        Gender gender,

        @NotBlank
        LocalDate birthDate,

        @NotBlank
        String nationality,

        @NotBlank
        String applyField,

        @NotBlank
        String profileImageUrl,

        @NotBlank
        String address,

        String specialty,

        String hobbies
) {
}
