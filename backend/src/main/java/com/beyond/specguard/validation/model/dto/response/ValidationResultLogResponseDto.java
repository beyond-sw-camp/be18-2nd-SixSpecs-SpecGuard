package com.beyond.specguard.validation.model.dto.response;

import com.beyond.specguard.validation.model.entity.ValidationResultLog;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ValidationResultLogResponseDto {


    private UUID id;
    private UUID resumeId;
    private Double validationScore;
    private String summary;
    private LocalDateTime validatedAt;
    private String descriptionComment;


    public ValidationResultLogResponseDto(
            UUID id,
            UUID resumeId,
            Double validationScore,
            LocalDateTime validatedAt,
            String descriptionComment
    ) {
        this.id = id;
        this.resumeId = resumeId;
        this.validationScore = validationScore;
        this.validatedAt = validatedAt;
        this.descriptionComment = descriptionComment;
    }





}
