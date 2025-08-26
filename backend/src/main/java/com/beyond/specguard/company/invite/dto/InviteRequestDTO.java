package com.beyond.specguard.company.invite.dto;

import com.beyond.specguard.company.invite.entity.InviteEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteRequestDTO {
    @NotBlank(message = "권한은 필수입니다.")
    private InviteEntity.InviteRole role;

    @NotBlank(message = "회사 ID는 필수입니다.")
    private String companyId;

    @Email
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;
}