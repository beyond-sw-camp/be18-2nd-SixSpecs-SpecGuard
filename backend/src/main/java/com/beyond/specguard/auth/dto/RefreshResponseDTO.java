package com.beyond.specguard.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefreshResponseDTO {
    private String accessToken;
    private String refreshToken;
}
