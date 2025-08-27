package com.beyond.specguard.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InviteCheckResponseDto {
    private String email;
    private String role;
    private String slug;
    private String companyName;
}