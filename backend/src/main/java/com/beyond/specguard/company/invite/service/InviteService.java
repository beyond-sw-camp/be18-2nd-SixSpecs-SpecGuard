package com.beyond.specguard.company.invite.service;

import com.beyond.specguard.company.invite.dto.InviteRequestDTO;
import com.beyond.specguard.company.invite.dto.InviteResponseDTO;
import com.beyond.specguard.company.invite.entity.InviteEntity;
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

    // ✅ application.properties 에서 도메인 주소 주입받기 (없으면 기본 localhost:8080 사용)
    @Value("${app.domain:http://localhost:8080}")
    private String appDomain;

    @Transactional
    public InviteResponseDTO sendInvite(InviteRequestDTO request) {
        // 1. 중복 체크
        if (inviteRepository.existsByEmailAndCompanyIdAndIsUsedFalse(
                request.getEmail(), request.getCompanyId())) {
            throw new RuntimeException("이미 초대된 이메일입니다."); // TODO: CustomException 처리
        }

        // 2. DB 저장
        InviteEntity invite = InviteEntity.builder()
                .companyId(request.getCompanyId())
                .email(request.getEmail())
                .role(request.getRole())
                .build();

        inviteRepository.save(invite);

        // 3. 초대 URL 생성
        String inviteUrl = "http://localhost:8080/api/v1/invite/accept?token=" + invite.getInviteToken();

        // 4. 메일 발송
        sendGridService.sendInviteEmail(invite.getEmail(), inviteUrl);

        // 5. 응답 반환 (최소 응답 스타일)
        return InviteResponseDTO.builder()
                .message("초대 메일이 발송되었습니다.")
                .inviteUrl(inviteUrl)
                .build();
    }
}
