package com.beyond.specguard.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshRequestDTO {

    @NotBlank(message = "refresh_token은 필수 입력값입니다.")
    @JsonProperty("refresh_token")   // ✅ snake_case ↔ camelCase 매핑
    private String refreshToken;
}
