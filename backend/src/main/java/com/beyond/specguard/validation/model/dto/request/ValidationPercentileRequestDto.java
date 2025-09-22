package com.beyond.specguard.validation.model.dto.request;

import com.beyond.specguard.resume.model.entity.Resume;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationPercentileRequestDto{
        @NotNull
        private UUID templateId;

        @NotNull
        private UUID resumeId;
}
