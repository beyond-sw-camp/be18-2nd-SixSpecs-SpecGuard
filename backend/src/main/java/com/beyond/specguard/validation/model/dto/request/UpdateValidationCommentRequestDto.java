package com.beyond.specguard.validation.model.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateValidationCommentRequestDto {

    @Size(max = 200, message=" 코멘트는 최대 200자까지 작성 가능합니다.")
    private String comment;
}
