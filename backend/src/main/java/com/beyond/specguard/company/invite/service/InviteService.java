package com.beyond.specguard.company.invite.service;

import com.beyond.specguard.auth.entity.ClientUser;
import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.common.exception.errorcode.InviteErrorCode;
import com.beyond.specguard.company.invite.dto.InviteRequestDTO;
import com.beyond.specguard.company.invite.dto.InviteResponseDTO;
import com.beyond.specguard.company.invite.entity.InviteEntity;
import com.beyond.specguard.company.invite.entity.InviteEntity.InviteStatus;
import com.beyond.specguard.company.invite.repository.InviteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InviteService {

    private final InviteRepository inviteRepository;
    private final SendGridService sendGridService;

    // ✅ application.properties에서 도메인 주소 주입 (기본값 localhost:8080)
    @Value("${app.domain:http://localhost:8080}")
    private String appDomain;

    @Transactional
    public InviteResponseDTO sendInvite(InviteRequestDTO request, ClientUser currentUser) {
        // 1. 권한 검증 (OWNER만 초대 가능)
        if (currentUser.getRole() != ClientUser.Role.OWNER) {
            throw new CustomException(InviteErrorCode.FORBIDDEN_INVITE);
        }

        // 2. 회사 소속 검증 (자기 회사 직원만 초대 가능)
        if (!currentUser.getCompany().getId().toString().equals(request.getCompanyId())) {
            throw new CustomException(InviteErrorCode.FORBIDDEN_INVITE);
        }

        // 3. 중복 초대 확인
        if (inviteRepository.existsByEmailAndCompanyIdAndStatus(
                request.getEmail(), request.getCompanyId(), InviteStatus.PENDING)) {
            throw new CustomException(InviteErrorCode.ALREADY_INVITED);
        }

        // 4. 초대 저장
        InviteEntity invite = InviteEntity.builder()
                .companyId(request.getCompanyId())
                .email(request.getEmail())
                .role(request.getRole())
                .status(InviteStatus.PENDING)
                .build();

        inviteRepository.save(invite);

        // 5. 초대 URL 생성 (단수 invite 사용)
        String inviteUrl = appDomain + "/api/v1/invite/accept?token=" + invite.getInviteToken();

        // 6. 메일 발송
        sendGridService.sendInviteEmail(invite.getEmail(), inviteUrl);

        // 7. 응답 반환
        return InviteResponseDTO.builder()
                .message("초대 메일이 발송되었습니다.")
                .inviteUrl(inviteUrl)
                .build();
    }
}
