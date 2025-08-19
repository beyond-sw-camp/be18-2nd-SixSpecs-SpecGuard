package com.beyond.specguard.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequestDTO {
    private String email;
    private String password;

    @Builder
    public LoginRequestDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
