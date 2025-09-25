package com.beyond.specguard.resume.model.dto.response;

import com.beyond.specguard.resume.model.entity.Resume;
import com.beyond.specguard.resume.model.entity.ResumeCertificate;
import com.beyond.specguard.resume.model.entity.ResumeEducation;
import com.beyond.specguard.resume.model.entity.ResumeExperience;
import com.beyond.specguard.validation.model.entity.ValidationResult;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;

@Builder
public record ResumeListResponseDto(
        List<Item> contents,
        Long totalElements,
        Integer totalPages,
        Integer pageNumber,
        Integer pageSize
) {
    public record Item(
            UUID templateId,                 //템플릿 ID
            UUID resumeId,                   // 이력서 식별용 ID
            String applicantName,            // 지원자 이름 (성+이니셜도 가능)
            String email,                     // 이메일 (기업 내부 용도, 마스킹 가능)
            String phone,                     // 전화번호 (마스킹 가능)
            Resume.ResumeStatus status,        // DRAFT, PENDING, SUBMITTED 등
            LocalDateTime lastUpdatedAt,      // 최근 업데이트일
            String highestEducation,          // 최종 학력 예: "서울대학교 컴퓨터공학과 BACHELOR"
            Integer totalExperienceYears,     // 총 경력 연차
            String latestPosition,            // 최근 직급/포지션
            List<String> skills,              // 지원자가 작성한 기술/스킬 리스트
            List<String> certifications,      // 자격증 리스트 (선택적)
            Boolean hasPortfolio,              // 포트폴리오/링크 존재 여부
            Double finalScore              //정합성 점수
    ) {
        public static Item fromEntity(Resume resume) {
            // 학력, 경력, 포트폴리오, 스킬 등 요약
            String highestEducation = resume.getResumeEducations().stream()
                    .max(Comparator.comparing(ResumeEducation::getEndDate))
                    .map(e -> e.getSchoolName() + " " + e.getMajor() + " " + e.getDegree())
                    .orElse("");

            Integer totalExperienceYears = resume.getResumeExperiences().stream()
                    .mapToInt(e -> {
                        if (e.getEndDate() != null) {
                            return Period.between(e.getStartDate(), e.getEndDate()).getYears();
                        } else {
                            return Period.between(e.getStartDate(), LocalDate.now()).getYears();
                        }
                    })
                    .sum();

            String latestPosition = resume.getResumeExperiences().stream()
                    .max(Comparator.comparing(ResumeExperience::getStartDate))
                    .map(ResumeExperience::getPosition)
                    .orElse("");


            String specialty = (resume.getResumeBasic() != null) ? resume.getResumeBasic().getSpecialty() : null;
            List<String> skills = (specialty == null || specialty.isBlank())
                    ? Collections.emptyList()
                    : Arrays.stream(specialty.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();

            List<String> certifications = resume.getResumeCertificates().stream()
                    .map(ResumeCertificate::getCertificateName)
                    .toList();

            Boolean hasPortfolio = !resume.getResumeLinks().isEmpty();

            Double finalScore = Optional.ofNullable(resume.getValidationResult())
                    .map(ValidationResult::getFinalScore)
                    .orElse(null);

            return new Item(
                    resume.getTemplate().getId(),
                    resume.getId(),
                    resume.getName(),
                    resume.getEmail(),
                    resume.getPhone(),
                    resume.getStatus(),
                    resume.getUpdatedAt(),
                    highestEducation,
                    totalExperienceYears,
                    latestPosition,
                    skills,
                    certifications,
                    hasPortfolio,
                    finalScore

            );
        }
    }
}
