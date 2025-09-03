package com.beyond.specguard.auth.model.dto;

import com.beyond.specguard.auth.model.entity.ClientCompany;
import com.beyond.specguard.auth.model.entity.ClientUser;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupResponseDto {

    private UserDTO user;
    private CompanyDTO company;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserDTO {
        private String id;
        private String name;
        private String email;
        private String phone;
        private String role;
        private String createdAt;

        // 정적 팩토리 메서드
        public static UserDTO from(ClientUser user) {
            return UserDTO.builder()
                    .id(user.getId().toString())
                    .name(user.getName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .role(user.getRole().name())
                    .createdAt(user.getCreatedAt() != null
                            ? user.getCreatedAt().toString()
                            : java.time.LocalDateTime.now().toString())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompanyDTO {
        private String id;
        private String name;
        private String slug;

        // 정적 팩토리 메서드
        public static CompanyDTO from(ClientCompany company) {
            return CompanyDTO.builder()
                    .id(company.getId().toString())
                    .name(company.getName())
                    .slug(company.getSlug())
                    .build();
        }
    }
}

