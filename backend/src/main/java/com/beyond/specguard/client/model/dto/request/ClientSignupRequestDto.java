package com.beyond.specguard.client.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "회원가입 요청 DTO", example = """
{
  "company": {
    "name": "비욘드소프트",
    "businessNumber": "1234567890",
    "slug": "beyondsoft",
    "managerName": "홍길동",
    "managerPosition": "팀장",
    "contactEmail": "manager@beyondsoft.com",
    "contactMobile": "010-1234-5678"
  },
  "user": {
    "email": "owner@beyondsoft.com",
    "password": "Test1234!",
    "name": "최고관리자",
    "phone": "010-9999-8888"
  }
}
""")
public class ClientSignupRequestDto {

    @NotNull(message = "회사 정보는 필수입니다.")
    @Schema(description = "회사 정보")
    private CompanyDTO company;

    @NotNull(message = "유저 정보는 필수입니다.")
    @Schema(description = "최초 사용자 정보")
    private UserDTO user;

    //  회사 정보
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "회사 정보 DTO")
    public static class CompanyDTO {

        @NotBlank(message = "회사명은 필수 입력 값입니다.")
        @Schema(description = "회사명", example = "비욘드소프트")
        private String name;

        @NotBlank(message = "사업자 등록번호는 필수 입력 값입니다.")
        @Size(min = 10, max = 12, message = "사업자 등록번호는 10~12자리여야 합니다.")
        @Schema(description = "사업자 등록번호", example = "1234567890")
        private String businessNumber;

        @Schema(description = "회사 식별용 슬러그", example = "beyondsoft")
        private String slug;

        @NotBlank(message = "담당자 이름은 필수 입력 값입니다.")
        @Schema(description = "담당자 이름", example = "홍길동")
        private String managerName;

        @Schema(description = "담당자 직책", example = "팀장")
        private String managerPosition;

        @Email(message = "담당자 이메일 형식이 올바르지 않습니다.")
        @Schema(description = "담당자 이메일", example = "manager@beyondsoft.com")
        private String contactEmail;

        @Schema(description = "담당자 휴대전화번호", example = "01012345678")
        private String contactMobile;
    }

    //  최초 사용자 정보
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "최초 사용자 정보 DTO")
    public static class UserDTO {

        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @NotBlank(message = "이메일은 필수 입력 값입니다.")
        @Schema(description = "사용자 이메일", example = "owner@beyondsoft.com")
        private String email;

        @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
                message = "비밀번호는 8~20자 영문, 숫자, 특수문자를 포함해야 합니다."
        )
        @Schema(description = "비밀번호", example = "Test1234!")
        private String password;

        @NotBlank(message = "이름은 필수 입력 값입니다.")
        @Schema(description = "사용자 이름", example = "최고관리자")
        private String name;

        @Schema(description = "휴대전화번호", example = "01099998888")
        private String phone;
    }
}
