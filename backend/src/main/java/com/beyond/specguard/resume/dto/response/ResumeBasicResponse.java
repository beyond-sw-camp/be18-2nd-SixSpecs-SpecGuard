package com.beyond.specguard.resume.dto.response;

import com.beyond.specguard.resume.entity.common.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

//API 응답으로 내려줄 dto
public record ResumeBasicResponse(
        @Schema(description = "기본정보 ID (UUID 문자열)")
        UUID id,
        @Schema(description = "이력서 ID (UUID 문자열)")
        UUID resumeId,

        String englishName,

        String gender,

        @Schema(type = "string", format = "date")
        LocalDate birthDate,

        String nationality,

        String address,

        String applyField,

        String specialty,

        String hobbies,

        @Schema(description = "저장된 프로필 이미지 URL")
        String profileImageUrl,

        LocalDateTime createdAt
) {
}
