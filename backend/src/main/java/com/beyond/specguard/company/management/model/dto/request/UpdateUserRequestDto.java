package com.beyond.specguard.company.management.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequestDto {

    private String name;

    private String phone;

}
