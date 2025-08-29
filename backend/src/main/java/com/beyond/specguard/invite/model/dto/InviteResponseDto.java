package com.beyond.specguard.invite.model.dto;

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