package com.beyond.specguard.company.invite.model.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteResponseDto {
    private String message;
    private String inviteUrl;
}