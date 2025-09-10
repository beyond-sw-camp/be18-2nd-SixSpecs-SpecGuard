package com.beyond.specguard.admin.model.dto;

import com.beyond.specguard.admin.model.entity.InternalAdmin;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@NoArgsConstructor
public class InternalAdminRequestDto {
    private String name;
    private String email;
    private String password;
    private InternalAdmin.Role role;
    private String phone;

    public void encodePassword(PasswordEncoder encoder) {
        this.password = encoder.encode(password);
    }

    public InternalAdmin toEntity() {
        return InternalAdmin.builder()
                .name(this.getName())
                .email(this.getEmail())
                .passwordHash(this.getPassword())
                .role(InternalAdmin.Role.ADMIN)
                .phone(this.getPhone())
                .build();
    }
}
