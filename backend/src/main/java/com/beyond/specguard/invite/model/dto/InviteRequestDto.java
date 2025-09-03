package com.beyond.specguard.invite.model.dto;

import com.beyond.specguard.invite.model.entity.InviteEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteRequestDto {
    @Schema(description = "초대할 사용자의 역할", example = "MANAGER")
    @NotNull(message = "역할(role)은 필수입니다.")
    private InviteEntity.InviteRole role;

    @Schema(description = "초대할 사용자의 이메일", example = "manager123@beyondsoft.com")
    @Email
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;
}