package com.beyond.specguard.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InviteCheckRequestDto {
    @Schema(example = "79fbff54-6b61-4d5e-804f-7330dd3ba223")
    @NotBlank(message = "초대 토큰은 필수입니다.")
    private String token;
}
