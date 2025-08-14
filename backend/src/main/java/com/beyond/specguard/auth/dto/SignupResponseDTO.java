package com.beyond.specguard.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupResponseDTO {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String createdAt;
}
