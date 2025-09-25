package com.beyond.specguard.company.management.model.service;

import com.beyond.specguard.auth.exception.errorcode.AuthErrorCode;
import com.beyond.specguard.company.management.model.dto.request.UpdateCompanyRequestDto;
import com.beyond.specguard.company.common.model.dto.response.SignupResponseDto;
import com.beyond.specguard.company.common.model.entity.ClientCompany;
import com.beyond.specguard.company.common.model.entity.ClientUser;
import com.beyond.specguard.company.common.model.repository.ClientCompanyRepository;
import com.beyond.specguard.company.common.model.repository.ClientUserRepository;
import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.company.management.model.dto.request.UpdateUserRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final ClientUserRepository userRepository;
    private final ClientCompanyRepository companyRepository;

    @Transactional
    public SignupResponseDto.CompanyDTO updateCompany(String slug, UpdateCompanyRequestDto dto, UUID userId) {

        // 회사 조회
        ClientCompany company = companyRepository.findBySlug(slug)
                .orElseThrow(() -> new CustomException(AuthErrorCode.COMPANY_NOT_FOUND));

        // 사용자 조회
        ClientUser clientUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        if (!clientUser.getCompany().getId().equals(company.getId())
                || clientUser.getRole() != ClientUser.Role.OWNER) {
            throw new CustomException(AuthErrorCode.ACCESS_DENIED);
        }

        // 회사 연락처, 담당자 직책 업데이트 (회사명은 제외)
        company.updateContactInfo(dto.getManagerPosition(), dto.getContactEmail(), dto.getContactMobile());

        // 유저 이름 업데이트
        if (dto.getName() != null && !dto.getName().isBlank()) {
            clientUser.update(
                    UpdateUserRequestDto.builder()
                            .name(dto.getName())
                            .build()
            );
        }

        return SignupResponseDto.CompanyDTO.from(company);
    }


    @Transactional
    public void deleteCompany(String slug, UUID userId) {
        //회사 조회
        ClientCompany company = companyRepository.findBySlug(slug)
                .orElseThrow(() -> new CustomException(AuthErrorCode.COMPANY_NOT_FOUND));
        // 사용자 조회
        ClientUser clientUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));
        // 권한이 OWNER인지
        if(!clientUser.getCompany().getId().equals(company.getId()) || clientUser.getRole() != ClientUser.Role.OWNER) {
            throw new CustomException(AuthErrorCode.ACCESS_DENIED);
        }
        //회사 내부에 있는 사원들까지 전부 탈퇴처리
        userRepository.deleteAllByCompanyId(company.getId());

        companyRepository.delete(company);
    }
}
