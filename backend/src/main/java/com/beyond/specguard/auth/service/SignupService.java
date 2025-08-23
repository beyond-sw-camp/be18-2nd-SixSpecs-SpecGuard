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

        SignupRequestDTO.CompanyDTO companyReq = request.getCompany();
        SignupRequestDTO.UserDTO userReq = request.getUser();

        // ✅ 사업자번호 중복 체크
        if (companyRepository.existsByBusinessNumber(companyReq.getBusinessNumber())) {
            throw new CustomException(AuthErrorCode.DUPLICATE_COMPANY);
        }

        // ✅ 이메일 중복 체크
        if (userRepository.existsByEmail(userReq.getEmail())) {
            throw new CustomException(AuthErrorCode.DUPLICATE_EMAIL);
        }

        // ✅ 회사 생성
        ClientCompany company = ClientCompany.builder()
                .name(companyReq.getName())
                .businessNumber(companyReq.getBusinessNumber())
                .slug(companyReq.getSlug())
                .managerPosition(companyReq.getManagerPosition())
                .managerName(companyReq.getManagerName())
                .contactEmail(companyReq.getContactEmail())
                .contactMobile(companyReq.getContactMobile())
                .build();
        companyRepository.save(company);

        // ✅ 최초 유저 생성
        ClientUser masterUser = ClientUser.builder()
                .company(company)
                .name(userReq.getName())
                .email(userReq.getEmail())
                .passwordHash(passwordEncoder.encode(userReq.getPassword()))
                .phone(userReq.getPhone())
                .role(ClientUser.Role.OWNER)
                .provider("local")
                .providerId(null)
                .profileImage(null)
                .build();
        ClientUser savedUser = userRepository.save(masterUser);

        // ✅ 응답 DTO 변환
        return SignupResponseDTO.builder()
                .user(SignupResponseDTO.UserDTO.builder()
                        .id(savedUser.getId().toString())
                        .name(savedUser.getName())
                        .email(savedUser.getEmail())
                        .phone(savedUser.getPhone())
                        .role(savedUser.getRole().name())
                        .createdAt(
                                savedUser.getCreatedAt() != null
                                        ? savedUser.getCreatedAt().toString()
                                        : java.time.LocalDateTime.now().toString()
                        )
                        .build())
                .company(SignupResponseDTO.CompanyDTO.builder()
                        .id(company.getId().toString())
                        .name(company.getName())
                        .slug(company.getSlug())
                        .build())
                .build();
    }
}
