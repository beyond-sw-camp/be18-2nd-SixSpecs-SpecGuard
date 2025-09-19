package com.beyond.specguard.validation.model.dto.response;

import com.beyond.specguard.validation.model.dto.request.ValidationPercentileRequestDto;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationPercentileResponseDto {
    @NotNull
    UUID resumeId;
    Double finalScore;
}