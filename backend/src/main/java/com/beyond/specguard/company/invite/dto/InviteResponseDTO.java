package com.beyond.specguard.company.invite.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class InviteResponseDTO {
    private String message;
    private String invite_code;
    private String invite_url;
}
