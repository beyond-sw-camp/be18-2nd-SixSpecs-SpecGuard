package com.beyond.specguard.resume.model.dto.response;

import com.beyond.specguard.resume.model.entity.ResumeLink;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ResumeLinkResponseDto(
        UUID id,
        String url,

        @NotNull
        ResumeLink.LinkType linkType,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
    public static ResumeLinkResponseDto fromEntity(ResumeLink resumeLink) {
        return ResumeLinkResponseDto.builder()
                .id(resumeLink.getId())
                .createdAt(resumeLink.getCreatedAt())
                .updatedAt(resumeLink.getUpdatedAt())
                .url(resumeLink.getUrl())
                .linkType(resumeLink.getLinkType())
                .build();
    }
}
