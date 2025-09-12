package com.beyond.specguard.auth.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompanyRequestDto {

    @Size(max = 20)
    private String name;

    @Size(max = 10)
    private String managerName;

    @Size(max = 10)
    private String managerPosition;

    @Email
    private String contactEmail;

    @Size(max = 20)
    private String contactMobile;
}
