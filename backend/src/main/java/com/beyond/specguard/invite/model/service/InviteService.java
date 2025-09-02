package com.beyond.specguard.invite.model.service;

import com.beyond.specguard.auth.model.entity.ClientCompany;
import com.beyond.specguard.auth.model.entity.ClientUser;
import com.beyond.specguard.auth.model.repository.ClientCompanyRepository;
import com.beyond.specguard.auth.model.service.CustomUserDetails;
import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.common.exception.errorcode.InviteErrorCode;
import com.beyond.specguard.common.jwt.JwtUtil;
import com.beyond.specguard.invite.model.dto.InviteRequestDto;
import com.beyond.specguard.invite.model.dto.InviteResponseDto;
import com.beyond.specguard.invite.model.entity.InviteEntity;
import com.beyond.specguard.invite.model.entity.InviteEntity.InviteStatus;
import com.beyond.specguard.invite.model.repository.InviteRepository;
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

    @Value("${invite.base-url}")
    private String inviteBaseUrl;

    @Transactional
    public InviteResponseDto sendInvite(String slug, InviteRequestDto request, CustomUserDetails currentUser) {
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

        // 4. 기존 PENDING 초대가 있으면 EXPIRED 처리
        inviteRepository.findByEmailAndCompanyAndStatus(request.getEmail(), company, InviteStatus.PENDING)
                .ifPresent(existingInvite -> {
                    existingInvite.setStatus(InviteStatus.EXPIRED);
                    existingInvite.setExpiresAt(LocalDateTime.now()); // 즉시 만료
                });

        // 5. 초대 토큰 생성 (JWT)
        String inviteToken = jwtUtil.createInviteToken(
                request.getEmail(),
                slug,
                request.getRole().name()
        );

        // 6. 새 엔티티 저장
        InviteEntity newInvite = InviteEntity.builder()
                .company(company) // FK 매핑
                .email(request.getEmail())
                .role(request.getRole())
                .status(InviteStatus.PENDING)
                .inviteToken(inviteToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        inviteRepository.save(newInvite);

        // 7. 메일 발송
        String inviteUrl = inviteBaseUrl + "?token=" + inviteToken;
        sendGridService.sendInviteEmail(newInvite.getEmail(), inviteUrl);

        // 8. 응답 반환
        return InviteResponseDto.builder()
                .message("초대 메일이 발송되었습니다.")
                .inviteUrl(inviteUrl)
                .build();
    }
}
