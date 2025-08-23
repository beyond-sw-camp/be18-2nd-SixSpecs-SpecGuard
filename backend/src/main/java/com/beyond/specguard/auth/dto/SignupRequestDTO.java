package com.beyond.specguard.auth.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequestDTO {

    @NotNull(message = "회사 정보는 필수입니다.")
    private CompanyDTO company;

    @NotNull(message = "유저 정보는 필수입니다.")
    private UserDTO user;

    // ✅ 회사 정보
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompanyDTO {
        @NotBlank(message = "회사명은 필수 입력 값입니다.")
        private String name;

        @NotBlank(message = "사업자 등록번호는 필수 입력 값입니다.")
        @Size(min = 10, max = 12, message = "사업자 등록번호는 10~12자리여야 합니다.")
        private String businessNumber;

        private String slug;

        @NotBlank(message = "담당자 이름은 필수 입력 값입니다.")
        private String managerName;

        private String managerPosition;

        @Email(message = "담당자 이메일 형식이 올바르지 않습니다.")
        private String contactEmail;

        private String contactMobile;
    }

    // ✅ 최초 사용자 정보
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserDTO {
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @NotBlank(message = "이메일은 필수 입력 값입니다.")
        private String email;

        @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
                message = "비밀번호는 8~20자 영문, 숫자, 특수문자를 포함해야 합니다."
        )
        private String password;

        @NotBlank(message = "이름은 필수 입력 값입니다.")
        private String name;

        private String phone;
    }
}
