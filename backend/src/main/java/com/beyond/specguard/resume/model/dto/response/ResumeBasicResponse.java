package com.beyond.specguard.resume.model.dto.response;

import com.beyond.specguard.resume.model.entity.core.ResumeBasic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

//API 응답으로 내려줄 dto
@Builder
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
        public static ResumeBasicResponse fromEntity(ResumeBasic resumeBasic) {
                return ResumeBasicResponse.builder()
                        .id(resumeBasic.getId())
                        .resumeId(resumeBasic.getResume().getId())
                        .englishName(resumeBasic.getEnglishName())
                        .gender(resumeBasic.getGender().name())
                        .birthDate(resumeBasic.getBirthDate())
                        .nationality(resumeBasic.getNationality())
                        .address(resumeBasic.getAddress())
                        .applyField(resumeBasic.getApplyField())
                        .specialty(resumeBasic.getSpecialty())
                        .hobbies(resumeBasic.getHobbies())
                        .profileImageUrl(resumeBasic.getProfileImageUrl())
                        .createdAt(resumeBasic.getCreatedAt())
                        .build();
        }
}
