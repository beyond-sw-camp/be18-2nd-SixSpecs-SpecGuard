package com.beyond.specguard.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequestDTO {

    // --- 회사 정보 ---
    @NotBlank(message = "회사 이름은 필수 입력 값입니다.")
    @Size(max = 50, message = "회사 이름은 최대 50자까지 가능합니다.")
    private String companyName;

    @NotBlank(message = "사업자번호는 필수 입력 값입니다.")
    @Size(max = 12, message = "사업자번호는 최대 12자까지 가능합니다.")
    private String businessNumber;

    @NotBlank(message = "회사 URL 슬러그는 필수 입력 값입니다.")
    @Size(max = 64, message = "슬러그는 최대 64자까지 가능합니다.")
    @Pattern(
            regexp = "^[a-z0-9-]+$",
            message = "슬러그는 소문자, 숫자, 하이픈(-)만 사용할 수 있습니다."
    )
    private String slug;

    @Size(max = 64, message = "직책은 최대 64자까지 가능합니다.")
    private String managerPosition;

    @Size(max = 30, message = "담당자 이름은 최대 30자까지 가능합니다.")
    private String managerName;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 100, message = "담당자 이메일은 최대 100자까지 가능합니다.")
    private String contactEmail;

    @Size(max = 100, message = "연락처는 최대 100자까지 가능합니다.")
    private String contactMobile;

    // --- 최초 유저(마스터) 정보 ---
    @NotBlank(message = "이름은 필수 입력 값입니다.")
    @Size(max = 100, message = "이름은 최대 100자까지 가능합니다.")
    private String name;

    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 255, message = "이메일은 최대 255자까지 가능합니다.")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "올바른 이메일 형식을 입력하세요."
    )
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Pattern(
            regexp = "^(?=.*[!@#$%^&*(),.?\":{}|<>])(?=.*[A-Za-z])(?=.*\\d).{8,}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다."
    )
    private String password;

    @Size(max = 50, message = "전화번호는 최대 50자까지 가능합니다.")
    @Pattern(
            regexp = "^(01[0-9])-?\\d{3,4}-?\\d{4}$",
            message = "올바른 전화번호 형식을 입력하세요. 예: 010-1234-5678"
    )
    private String phone;
}
