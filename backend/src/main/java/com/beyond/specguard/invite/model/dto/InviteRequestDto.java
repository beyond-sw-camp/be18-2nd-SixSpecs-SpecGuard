package com.beyond.specguard.invite.model.dto;

import com.beyond.specguard.invite.model.entity.InviteEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteRequestDto {
    @NotNull(message = "역할(role)은 필수입니다.")
    private InviteEntity.InviteRole role;

    @Email
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;
}