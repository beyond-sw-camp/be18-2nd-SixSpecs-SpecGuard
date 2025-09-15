package com.beyond.specguard.admin.model.dto.response;

import com.beyond.specguard.admin.model.entity.InternalAdmin;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InternalAdminResponseDto {
    private UUID id;
    private String name;
    private String email;
    private InternalAdmin.Role role;
    private String phone;

    public static InternalAdminResponseDto fromEntity(InternalAdmin entity) {
        return InternalAdminResponseDto.builder()
                .name(entity.getName())
                .id(entity.getId())
                .email(entity.getEmail())
                .role(entity.getRole())
                .phone(entity.getPhone())
                .build();
    }
}
