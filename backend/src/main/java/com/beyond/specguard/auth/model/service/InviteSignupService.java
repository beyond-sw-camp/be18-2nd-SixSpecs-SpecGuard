package com.beyond.specguard.auth.model.service;

import com.beyond.specguard.auth.model.dto.InviteCheckResponseDto;
import com.beyond.specguard.auth.model.dto.InviteSignupRequestDto;
import com.beyond.specguard.auth.model.dto.SignupResponseDto;
import com.beyond.specguard.auth.model.entity.ClientCompany;
import com.beyond.specguard.auth.model.entity.ClientUser;
import com.beyond.specguard.auth.model.repository.ClientUserRepository;
import com.beyond.specguard.invite.exception.InviteException;
import com.beyond.specguard.invite.exception.errorcode.InviteErrorCode;
import com.beyond.specguard.invite.model.entity.InviteEntity;
import com.beyond.specguard.invite.model.repository.InviteRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InviteSignupService {

    private final InviteRepository inviteRepository;
    private final ClientUserRepository clientUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    @Transactional
    public SignupResponseDto signupWithInvite(InviteSignupRequestDto dto) {
        // 1. 초대 토큰 조회
        InviteEntity invite = inviteRepository.findByInviteTokenAndStatus(
                dto.getToken(),
                InviteEntity.InviteStatus.PENDING
        ).orElseThrow(() -> new InviteException(InviteErrorCode.INVALID_TOKEN));

        // 2. 만료 여부 확인
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            invite.setStatus(InviteEntity.InviteStatus.EXPIRED);
            throw new InviteException(InviteErrorCode.EXPIRED_TOKEN);
        }

        // 3. 회사 조회 (FK 매핑으로 단순화)
        ClientCompany company = invite.getCompany();

        // 4. 유저 생성
        ClientUser newUser = ClientUser.builder()
                .company(company)
                .email(invite.getEmail()) // 초대 이메일 고정
                .name(dto.getName())
                .phone(dto.getPhone())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .role(ClientUser.Role.valueOf(invite.getRole().name()))
                .provider("local")
                .providerId(null)
                .profileImage(null)
                .build();

        clientUserRepository.save(newUser);
        entityManager.flush();
        entityManager.refresh(newUser);

        // 5. 초대 상태 갱신
        invite.setStatus(InviteEntity.InviteStatus.ACCEPTED);

        return SignupResponseDto.builder()
                .user(SignupResponseDto.UserDTO.from(newUser))
                .company(SignupResponseDto.CompanyDTO.from(company))
                .build();
    }

    @Transactional(readOnly = true)
    public InviteCheckResponseDto checkInvite(String token) {
        InviteEntity invite = inviteRepository.findByInviteTokenAndStatus(
                token, InviteEntity.InviteStatus.PENDING
        ).orElseThrow(() -> new InviteException(InviteErrorCode.INVALID_TOKEN));

        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InviteException(InviteErrorCode.EXPIRED_TOKEN);
        }

        ClientCompany company = invite.getCompany();

        return InviteCheckResponseDto.builder()
                .email(invite.getEmail())
                .role(invite.getRole().name())
                .slug(company.getSlug())
                .companyName(company.getName())
                .build();
    }
}