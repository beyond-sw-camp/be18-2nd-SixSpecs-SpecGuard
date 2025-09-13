package com.beyond.specguard.auth.model.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReissueResponseDto {
    private String accessToken;
    private String refreshToken;
    private String message;   //  필드 선언은 클래스 레벨
}
