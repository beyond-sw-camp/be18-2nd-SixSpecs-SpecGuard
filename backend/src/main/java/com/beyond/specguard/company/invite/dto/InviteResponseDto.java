package com.beyond.specguard.company.invite.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteResponseDto {
    private String message;
    private String inviteUrl;
}