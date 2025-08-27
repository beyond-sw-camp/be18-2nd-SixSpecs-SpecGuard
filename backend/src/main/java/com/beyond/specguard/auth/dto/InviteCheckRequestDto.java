package com.beyond.specguard.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InviteCheckRequestDto {
    @NotBlank(message = "초대 토큰은 필수입니다.")
    private String token;
}
