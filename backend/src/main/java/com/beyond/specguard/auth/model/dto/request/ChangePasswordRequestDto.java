package com.beyond.specguard.auth.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequestDto {

    @NotBlank(message = "현재 비밀번호를 입력하세요")
    private String oldPassword;

    @NotBlank(message = "변경하실 비밀번호를 입력하세요")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
            message = "비밀번호는 8~20자 영문, 숫자, 특수문자를 포함해야 합니다."
    )
    private String newPassword;
}
