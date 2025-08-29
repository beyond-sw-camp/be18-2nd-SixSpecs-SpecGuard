package com.beyond.specguard.auth.model.service;

import com.beyond.specguard.auth.model.dto.InviteCheckResponseDto;
import com.beyond.specguard.auth.model.dto.InviteSignupRequestDto;
import com.beyond.specguard.auth.model.entity.ClientCompany;
import com.beyond.specguard.auth.model.entity.ClientUser;
import com.beyond.specguard.auth.model.repository.ClientCompanyRepository;
import com.beyond.specguard.auth.model.repository.ClientUserRepository;
import com.beyond.specguard.invite.model.entity.InviteEntity;
import com.beyond.specguard.invite.model.repository.InviteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InviteSignupService {

    private final InviteRepository inviteRepository;
    private final ClientUserRepository clientUserRepository;
    private final ClientCompanyRepository clientCompanyRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ClientUser signupWithInvite(String inviteToken, InviteSignupRequestDto dto) {
        // 1. 초대 토큰 조회
        InviteEntity invite = inviteRepository.findByInviteTokenAndStatus(
                inviteToken,
                InviteEntity.InviteStatus.PENDING
        ).orElseThrow(() -> new IllegalArgumentException("유효하지 않거나 이미 처리된 초대입니다."));

        // 2. 만료 여부 확인
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            invite.setStatus(InviteEntity.InviteStatus.EXPIRED);
            inviteRepository.save(invite);
            throw new IllegalStateException("초대가 만료되었습니다.");
        }

        // 3. 회사 조회
        ClientCompany company = clientCompanyRepository.findById(UUID.fromString(invite.getCompanyId()))
                .orElseThrow(() -> new IllegalStateException("회사를 찾을 수 없습니다."));

        // 4. 유저 생성 (빌더 패턴)
        ClientUser newUser = ClientUser.builder()
                .company(company)
                .email(invite.getEmail()) //  초대 이메일 고정
                .name(dto.getName())
                .phone(dto.getPhone())
                .passwordHash(dto.getPassword() != null ? passwordEncoder.encode(dto.getPassword()) : null)
                .role(ClientUser.Role.valueOf(invite.getRole().name()))
                .provider("local")
                .providerId(null)
                .profileImage(null)
                .build();

        clientUserRepository.save(newUser);

        // 5. 초대 상태 갱신
        invite.setStatus(InviteEntity.InviteStatus.ACCEPTED);
        inviteRepository.save(invite);

        return newUser;
    }
    @Transactional(readOnly = true)
    public InviteCheckResponseDto checkInvite(String inviteToken) {
        InviteEntity invite = inviteRepository.findByInviteTokenAndStatus(
                inviteToken, InviteEntity.InviteStatus.PENDING
        ).orElseThrow(() -> new IllegalArgumentException("유효하지 않거나 이미 처리된 초대입니다."));

        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("초대가 만료되었습니다.");
        }

        ClientCompany company = clientCompanyRepository.findById(UUID.fromString(invite.getCompanyId()))
                .orElseThrow(() -> new IllegalStateException("회사를 찾을 수 없습니다."));

        return InviteCheckResponseDto.builder()
                .email(invite.getEmail())
                .role(invite.getRole().name())
                .slug(company.getSlug())
                .companyName(company.getName())
                .build();
    }
}