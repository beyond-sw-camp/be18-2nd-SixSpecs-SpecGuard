package com.beyond.specguard.auth.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponseDto {
    private String accessToken;
    private String message;
}
