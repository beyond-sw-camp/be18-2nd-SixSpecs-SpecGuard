package com.beyond.specguard.resume.model.dto.response;

import com.beyond.specguard.resume.model.entity.CompanyFormSubmission;
import com.beyond.specguard.resume.model.entity.Resume;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ResumeSubmitResponse(
        @Schema(description = "제출 이력 ID")
        UUID submissionId,
        UUID resumeId,
        UUID companyId,
        @Schema(description = "제출 시각")
        LocalDateTime submittedAt,
        @Schema(description = "이력서 현재 상태")
        Resume.ResumeStatus status
) {
        public static ResumeSubmitResponse fromEntity(CompanyFormSubmission submission) {
                return ResumeSubmitResponse.builder()
                        .submissionId(submission.getId())
                        .resumeId(submission.getResume().getId())
                        .companyId(submission.getCompanyId())
                        .submittedAt(submission.getSubmittedAt())
                        .status(submission.getResume().getStatus())
                        .build();
        }
}
