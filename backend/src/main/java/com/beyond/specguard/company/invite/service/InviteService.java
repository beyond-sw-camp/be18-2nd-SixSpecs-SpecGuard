package com.beyond.specguard.company.invite.service;

import com.beyond.specguard.auth.entity.ClientCompany;
import com.beyond.specguard.auth.entity.ClientUser;
import com.beyond.specguard.auth.repository.ClientCompanyRepository;
import com.beyond.specguard.auth.service.CustomUserDetails;
import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.common.exception.errorcode.InviteErrorCode;
import com.beyond.specguard.common.jwt.JwtUtil;
import com.beyond.specguard.company.invite.dto.InviteRequestDTO;
import com.beyond.specguard.company.invite.dto.InviteResponseDTO;
import com.beyond.specguard.company.invite.entity.InviteEntity;
import com.beyond.specguard.company.invite.entity.InviteEntity.InviteStatus;
import com.beyond.specguard.company.invite.repository.InviteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InviteService {

    private final InviteRepository inviteRepository;
    private final ClientCompanyRepository companyRepository;
    private final SendGridService sendGridService;
    private final JwtUtil jwtUtil;

    @Value("${app.domain:http://localhost:8080}")
    private String appDomain;

    @Transactional
    public InviteResponseDTO sendInvite(String slug, InviteRequestDTO request, CustomUserDetails currentUser) {
        // 1. 권한 검증 (OWNER만 초대 가능)
        if (currentUser.getUser().getRole() != ClientUser.Role.OWNER) {
            throw new CustomException(InviteErrorCode.FORBIDDEN_INVITE);
        }

        // 2. slug → company 조회
        ClientCompany company = companyRepository.findBySlug(slug)
                .orElseThrow(() -> new CustomException(InviteErrorCode.COMPANY_NOT_FOUND));

        // 3. 회사 소속 검증
        if (!currentUser.getCompany().getId().equals(company.getId())) {
            throw new CustomException(InviteErrorCode.FORBIDDEN_INVITE);
        }

        // 4. 중복 초대 확인
        if (inviteRepository.existsByEmailAndCompanyIdAndStatus(
                request.getEmail(), company.getId().toString(), InviteStatus.PENDING)) {
            throw new CustomException(InviteErrorCode.ALREADY_INVITED);
        }

        // 5. 초대 토큰 생성 (JWT)
        String inviteToken = jwtUtil.createInviteToken(
                request.getEmail(),
                slug,
                request.getRole().name()
        );

        InviteEntity invite = InviteEntity.builder()
                .companyId(company.getId().toString())
                .email(request.getEmail())
                .role(request.getRole())
                .status(InviteStatus.PENDING)
                .inviteToken(inviteToken)
                .expiresAt(LocalDateTime.now().plusDays(7)) // or JWT exp와 동일
                .build();
        inviteRepository.save(invite);

        // 7. 초대 URL 생성
        String inviteUrl = appDomain + "/api/v1/companies/" + slug + "/invites/accept?token=" + inviteToken;

        // 8. 메일 발송
        sendGridService.sendInviteEmail(invite.getEmail(), inviteUrl);

        // 9. 응답 반환
        return InviteResponseDTO.builder()
                .message("초대 메일이 발송되었습니다.")
                .inviteUrl(inviteUrl)
                .build();
    }
}
