package com.beyond.specguard.auth.model.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ClientLoginRequestDto {
    private String email;
    private String password;

    @Builder
    public ClientLoginRequestDto(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
