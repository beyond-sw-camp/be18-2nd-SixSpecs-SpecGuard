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

        // 0. 필수값 검증 (Owner는 로컬 계정이므로 비밀번호/이메일/이름이 반드시 있어야 함)
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new CustomException(AuthErrorCode.INVALID_EMAIL_FORMAT);
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new CustomException(AuthErrorCode.WEAK_PASSWORD);
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new CustomException(AuthErrorCode.INVALID_LOGIN); // 이름 필수 에러코드 따로 만들어도 됨
        }

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

        // 3. 최초 유저 생성 (OWNER 권한, 로컬 계정)
        ClientUser masterUser = ClientUser.builder()
                .company(company)
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(ClientUser.Role.OWNER) // 최초 가입자는 OWNER
                .provider("local")           // ✅ 로컬 계정 명시
                .providerId(null)            // ✅ 소셜 계정 아님
                .profileImage(null)          // ✅ 필요 시 기본 이미지 넣어도 됨
                .build();
        ClientUser savedUser = userRepository.save(masterUser);

        //응답 dto 변환 null safe 옵션 추가
        // 응답 DTO 변환
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
                                        : java.time.LocalDateTime.now().toString() // ✅ fallback: 현재 시간
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
