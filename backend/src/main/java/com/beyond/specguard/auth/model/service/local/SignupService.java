<<<<<<< HEAD:backend/src/main/java/com/beyond/specguard/auth/model/service/SignupService.java
package com.beyond.specguard.auth.model.service;

import com.beyond.specguard.auth.model.dto.request.SignupRequestDto;
import com.beyond.specguard.auth.model.dto.response.SignupResponseDto;
import com.beyond.specguard.auth.model.entity.ClientCompany;
import com.beyond.specguard.auth.model.entity.ClientUser;
import com.beyond.specguard.auth.model.repository.ClientCompanyRepository;
import com.beyond.specguard.auth.model.repository.ClientUserRepository;
import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.auth.exception.errorcode.AuthErrorCode;
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
    public SignupResponseDto signup(SignupRequestDto request) {
        SignupRequestDto.CompanyDTO companyReq = request.getCompany();
        SignupRequestDto.UserDTO userReq = request.getUser();

        // [회사 검증]
        if (companyRepository.existsByBusinessNumber(companyReq.getBusinessNumber())) {
            throw new CustomException(AuthErrorCode.DUPLICATE_COMPANY);
        }
        if (companyRepository.existsBySlug(companyReq.getSlug())) {
            throw new CustomException(AuthErrorCode.DUPLICATE_SLUG);
        }
        if (companyRepository.existsByName(companyReq.getName())) {
            throw new CustomException(AuthErrorCode.DUPLICATE_COMPANY_NAME);
        }

        // [유저 검증]
        if (userRepository.existsByEmail(userReq.getEmail())) {
            throw new CustomException(AuthErrorCode.DUPLICATE_EMAIL);
        }


        // [회사 생성]
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

        if (userRepository.existsByEmailAndCompany_Id(userReq.getEmail(), company.getId())) {
            throw new CustomException(AuthErrorCode.DUPLICATE_EMAIL_IN_COMPANY);
        }
        // [최초 유저 생성]
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

        // [응답 반환]
        return SignupResponseDto.builder()
                .user(SignupResponseDto.UserDTO.from(savedUser))
                .company(SignupResponseDto.CompanyDTO.from(company))
                .build();
    }
}
=======
package com.beyond.specguard.auth.model.service.local;

import com.beyond.specguard.auth.model.dto.request.SignupRequestDto;
import com.beyond.specguard.auth.model.dto.response.SignupResponseDto;
import com.beyond.specguard.auth.model.entity.ClientCompany;
import com.beyond.specguard.auth.model.entity.ClientUser;
import com.beyond.specguard.auth.model.repository.ClientCompanyRepository;
import com.beyond.specguard.auth.model.repository.ClientUserRepository;
import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.auth.exception.errorcode.AuthErrorCode;
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
    public SignupResponseDto signup(SignupRequestDto request) {
        SignupRequestDto.CompanyDTO companyReq = request.getCompany();
        SignupRequestDto.UserDTO userReq = request.getUser();

        // [회사 검증]
        if (companyRepository.existsByBusinessNumber(companyReq.getBusinessNumber())) {
            throw new CustomException(AuthErrorCode.DUPLICATE_COMPANY);
        }
        if (companyRepository.existsBySlug(companyReq.getSlug())) {
            throw new CustomException(AuthErrorCode.DUPLICATE_SLUG);
        }
        if (companyRepository.existsByName(companyReq.getName())) {
            throw new CustomException(AuthErrorCode.DUPLICATE_COMPANY_NAME);
        }

        // [유저 검증]
        if (userRepository.existsByEmail(userReq.getEmail())) {
            throw new CustomException(AuthErrorCode.DUPLICATE_EMAIL);
        }


        // [회사 생성]
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

        if (userRepository.existsByEmailAndCompany_Id(userReq.getEmail(), company.getId())) {
            throw new CustomException(AuthErrorCode.DUPLICATE_EMAIL_IN_COMPANY);
        }
        // [최초 유저 생성]
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

        // [응답 반환]
        return SignupResponseDto.builder()
                .user(SignupResponseDto.UserDTO.from(savedUser))
                .company(SignupResponseDto.CompanyDTO.from(company))
                .build();
    }
}
>>>>>>> develop:backend/src/main/java/com/beyond/specguard/auth/model/service/local/SignupService.java
