package com.beyond.specguard.company.common.model.dto.response;

import com.beyond.specguard.company.common.model.entity.ClientCompany;
import com.beyond.specguard.company.common.model.entity.ClientUser;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupResponseDto {

    private UserDTO user;
    private CompanyDTO company;
    private List<UserDTO> employees; // 오너일 때만 세팅, 일반 유저는 null 또는 빈 리스트


    @Getter
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
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompanyDTO {
        private String id;
        private String name;
        private String slug;
        private String businessNumber;
        private String managerName;
        private String managerPosition;
        private String contactEmail;
        private String contactMobile;


        // 정적 팩토리 메서드
        public static CompanyDTO from(ClientCompany company) {
            return CompanyDTO.builder()
                    .id(company.getId().toString())
                    .name(company.getName())
                    .slug(company.getSlug())
                    .businessNumber(company.getBusinessNumber())
                    .managerName(company.getManagerName())
                    .managerPosition(company.getManagerPosition())
                    .contactEmail(company.getContactEmail())
                    .contactMobile(company.getContactMobile())
                    .build();
        }
    }
}

