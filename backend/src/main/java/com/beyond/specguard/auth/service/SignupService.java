package com.beyond.specguard.auth.service;

import com.beyond.specguard.auth.dto.SignupRequestDTO;
import com.beyond.specguard.auth.dto.SignupResponseDTO;
import com.beyond.specguard.auth.entity.ClientCompany;
import com.beyond.specguard.auth.entity.ClientUser;
import com.beyond.specguard.auth.repository.ClientCompanyRepository;
import com.beyond.specguard.auth.repository.ClientUserRepository;
import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.common.exception.errorcode.AuthErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final ClientCompanyRepository companyRepository;
    private final ClientUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponseDTO signup(SignupRequestDTO request) {

        // 1. 사업자번호 중복 체크
        if (companyRepository.existsByBusinessNumber(request.getBusinessNumber())) {
            throw new CustomException(AuthErrorCode.DUPLICATE_COMPANY);
        }

        // 2. 회사 생성
        ClientCompany company = ClientCompany.builder()
                .name(request.getCompanyName())
                .businessNumber(request.getBusinessNumber())
                .slug(request.getSlug())
                .managerPosition(request.getManagerPosition())
                .managerName(request.getManagerName())
                .contactEmail(request.getContactEmail())
                .contactMobile(request.getContactMobile())
                .build();
        companyRepository.save(company);

        // 3. 최초 유저 생성 (MASTER 권한)
        ClientUser masterUser = ClientUser.builder()
                .company(company)
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(ClientUser.Role.OWNER) // 최초 가입자는 OWNER
                .build();
        ClientUser savedUser = userRepository.save(masterUser);

        // 4. 응답 DTO 변환
        return SignupResponseDTO.builder()
                .user(SignupResponseDTO.UserDTO.builder()
                        .id(savedUser.getId().toString())
                        .name(savedUser.getName())
                        .email(savedUser.getEmail())
                        .phone(savedUser.getPhone())
                        .role(savedUser.getRole().name())
                        .createdAt(savedUser.getCreatedAt().toString())
                        .build())
                .company(SignupResponseDTO.CompanyDTO.builder()
                        .id(company.getId().toString())
                        .name(company.getName())
                        .slug(company.getSlug())
                        .build())
                .build();

    }
}
