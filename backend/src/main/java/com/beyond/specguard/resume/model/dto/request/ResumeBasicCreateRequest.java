package com.beyond.specguard.resume.model.dto.request;

import com.beyond.specguard.resume.model.entity.Resume;
import com.beyond.specguard.resume.model.entity.ResumeBasic;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ResumeBasicCreateRequest(
        @Schema(description = "영문 이름", example = "Hong GilDong")
        @NotBlank
        String englishName,

        @Schema(description = "성별", example = "M")
        @NotNull
        ResumeBasic.Gender gender,

        @Schema(description = "생년월일", type = "string", format = "date", example = "1995-03-10")
        @NotNull
        LocalDate birthDate,

        @Schema(description = "국적", example = "Korean")
        @NotBlank
        String nationality,

        @Schema(description = "주소", example = "Seoul, South Korea")
        @NotBlank
        String address,

        @Schema(description = "우편번호", example = "16538")
        @NotBlank
        String zip,

        @Schema(description = "특기", example = "프론트엔드 개발")
        String specialty,

        @Schema(description = "취미", example = "등산, 독서")
        String hobbies
) {
        public ResumeBasic toEntity(Resume resume) {
                return ResumeBasic.builder()
                        .resume(resume)
                        .englishName(englishName)
                        .gender(gender)
                        .birthDate(birthDate)
                        .nationality(nationality)
                        .zip(zip)
                        .address(address)
                        .specialty(specialty)
                        .hobbies(hobbies)
                        .build();
        }
}
