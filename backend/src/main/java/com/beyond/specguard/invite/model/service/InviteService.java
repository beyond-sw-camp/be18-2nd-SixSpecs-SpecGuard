package com.beyond.specguard.invite.model.service;

import com.beyond.specguard.auth.model.entity.ClientCompany;
import com.beyond.specguard.auth.model.entity.ClientUser;
import com.beyond.specguard.auth.model.repository.ClientCompanyRepository;
import com.beyond.specguard.auth.model.repository.ClientUserRepository;
import com.beyond.specguard.auth.model.service.local.CustomUserDetails;
import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.invite.exception.errorcode.InviteErrorCode;
import com.beyond.specguard.common.util.JwtUtil;
import com.beyond.specguard.invite.model.dto.request.InviteRequestDto;
import com.beyond.specguard.invite.model.dto.response.InviteResponseDto;
import com.beyond.specguard.invite.model.entity.InviteEntity;
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
    private final InviteSendGridService inviteSendGridService;
    private final JwtUtil jwtUtil;
    private final ClientUserRepository userRepository;

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

        if(currentUser.getUser().getEmail().equalsIgnoreCase(request.getEmail())) {
            throw new CustomException(InviteErrorCode.SELF_INVITE_NOT_ALLOWED);
        }
        // 4. 서비스 내부에 이미 존재하는 이메일에 초대코드를 보내려고 할때 예외처리
        if (userRepository.existsByEmailAndCompany_Id(request.getEmail(), company.getId())) {
            throw new CustomException(InviteErrorCode.ALREADY_REGISTERED);
        }

        // 4. 기존 PENDING 초대 → EXPIRED 처리
        inviteRepository.findByEmailAndCompanyAndStatus(request.getEmail(), company, InviteEntity.InviteStatus.PENDING)
                .ifPresent(existingInvite -> {
                    existingInvite.setStatus(InviteEntity.InviteStatus.EXPIRED);
                    existingInvite.setExpiresAt(LocalDateTime.now()); // 즉시 만료
                });

        // 5. 초대 토큰 생성
        String inviteToken = jwtUtil.createInviteToken(
                request.getEmail(),
                slug,
                request.getRole().name()
        );

        // 6. 엔티티 저장
        InviteEntity newInvite = InviteEntity.builder()
                .company(company)
                .email(request.getEmail())
                .role(request.getRole())
                .status(InviteEntity.InviteStatus.PENDING)
                .inviteToken(inviteToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        inviteRepository.save(newInvite);

        // 7. 최종 초대 URL 조립 (InviteService에서만)
        String inviteUrl = inviteBaseUrl + "?token=" + inviteToken;

        // 8. 메일 발송
        inviteSendGridService.sendInviteEmail(newInvite.getEmail(), inviteUrl);

        // 9. 응답 반환
        return InviteResponseDto.builder()
                .message("초대 메일이 발송되었습니다.")
                .inviteUrl(inviteUrl)
                .build();
    }
}
