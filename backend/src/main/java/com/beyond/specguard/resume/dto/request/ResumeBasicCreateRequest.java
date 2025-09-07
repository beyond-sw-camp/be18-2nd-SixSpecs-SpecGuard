package com.beyond.specguard.resume.dto.request;

import com.beyond.specguard.resume.entity.common.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ResumeBasicCreateRequest(
        @Schema(description = "영문 이름", example = "John Doe")
        @NotNull
        String englishName,

        @Schema(description = "성별", example = "M")
        @NotNull
        Gender gender,

        @Schema(description = "생년월일", type = "string", format = "date", example = "1995-03-10")
        @NotNull
        LocalDate birthDate,

        @Schema(description = "국적", example = "Korean")
        @NotBlank
        String nationality,

        @Schema(description = "주소", example = "Seoul, South Korea")
        @NotBlank
        String address,

        @Schema(description = "지원 분야", example = "웹 개발")
        @NotBlank
        String applyField,

        @Schema(description = "특기", example = "프론트엔드 개발")
        String specialty,

        @Schema(description = "취미", example = "등산, 독서")
        String hobbies,

        @Schema(description = "프로필 이미지 파일")
        @NotNull
        String profileImage
) {
}
