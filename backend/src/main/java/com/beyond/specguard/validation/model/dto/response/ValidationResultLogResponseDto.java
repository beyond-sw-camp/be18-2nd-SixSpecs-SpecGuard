package com.beyond.specguard.validation.model.dto.response;

import com.beyond.specguard.validation.model.entity.ValidationResultLog;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ValidationResultLogResponseDto {


    private UUID id;
    private UUID resumeId;
    private Double validationScore;
    private LocalDateTime validatedAt;
    private String descriptionComment;

}
