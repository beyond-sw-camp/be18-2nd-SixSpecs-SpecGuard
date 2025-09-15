package com.beyond.specguard.company.common.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "초대 회원 가입 요청 DTO")
public class InviteSignupRequestDto {

    @Schema(description = "초대 토큰", example = "79fbff54-6b61-4d5e-804f-7330dd3ba223")
    @NotBlank(message = "초대 토큰은 필수입니다.")
    private String token;

    @Schema(description = "사용자 이름", example = "홍길동")
    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @Schema(description = "비밀번호", example = "Test5678!")
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
            message = "비밀번호는 8~20자 영문, 숫자, 특수문자를 포함해야 합니다."
    )
    private String password;

    @Schema(description = "전화번호", example = "01012345678")
    @NotBlank(message = "전화번호는 필수입니다.")
    private String phone;
}