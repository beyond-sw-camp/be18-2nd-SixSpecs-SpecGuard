package com.beyond.specguard.tempresume;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeResponseDto {
    private String id;
    private String templateId;
    private String name;
    private String phone;
    private String email;
    private String status;
}