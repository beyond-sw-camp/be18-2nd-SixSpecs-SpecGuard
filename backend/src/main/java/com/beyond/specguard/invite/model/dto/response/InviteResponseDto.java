package com.beyond.specguard.invite.model.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteResponseDto {
    private String message;
    private String inviteUrl;
}