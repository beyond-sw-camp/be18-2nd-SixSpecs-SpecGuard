package com.beyond.specguard.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteSignupRequestDto {
    private String name;
    private String password;
    private String phone;
}