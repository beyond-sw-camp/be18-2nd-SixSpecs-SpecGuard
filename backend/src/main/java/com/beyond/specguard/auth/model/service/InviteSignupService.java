package com.beyond.specguard.auth.model.service;

import com.beyond.specguard.auth.model.dto.request.InviteSignupRequestDto;
import com.beyond.specguard.auth.model.dto.response.InviteCheckResponseDto;
import com.beyond.specguard.auth.model.dto.response.SignupResponseDto;
import com.beyond.specguard.auth.model.entity.ClientCompany;
import com.beyond.specguard.auth.model.entity.ClientUser;
import com.beyond.specguard.auth.model.repository.ClientUserRepository;
import com.beyond.specguard.invite.exception.InviteException;
import com.beyond.specguard.invite.exception.errorcode.InviteErrorCode;
import com.beyond.specguard.invite.model.entity.InviteEntity;
import com.beyond.specguard.invite.model.repository.InviteRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
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
            invite.inviteExpired();
            throw new InviteException(InviteErrorCode.EXPIRED_TOKEN);
        }

        // 3. 회사 조회 (FK조건 빼버렸으니까 세부적으로 찾았음)
        ClientCompany company = invite.getCompany();
        if (company == null) {
            throw new InviteException(InviteErrorCode.COMPANY_NOT_FOUND);
        }

        // 4. 이미 가입된 이메일인지 검증(2차 방어선 만약 초대발송때 못막으면 여기서 막힘)
        if (clientUserRepository.existsByEmailAndCompany_Id(invite.getEmail(), company.getId())) {
            throw new InviteException(InviteErrorCode.ALREADY_REGISTERED);
        }

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
        invite.inviteAccepted();

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
        log.info("✅ checkInvite API - DB에서 찾은 invite_token: {}", invite.getInviteToken());

        return InviteCheckResponseDto.builder()
                .email(invite.getEmail())
                .role(invite.getRole().name())
                .slug(company.getSlug())
                .companyName(company.getName())
                .token(invite.getInviteToken()) // ✅ DB에 저장된 JWT를 그대로 내려줌
                .build();
    }
}