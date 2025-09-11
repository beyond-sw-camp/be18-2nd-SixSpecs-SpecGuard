package com.beyond.specguard.invite.model.service;

import com.beyond.specguard.common.util.JwtUtil;
import com.beyond.specguard.invite.exception.InviteException;
import com.beyond.specguard.invite.exception.errorcode.InviteErrorCode;
import com.beyond.specguard.invite.model.entity.InviteEntity;
import com.beyond.specguard.invite.model.repository.InviteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class InviteValidator {

    private final InviteRepository inviteRepository;
    private final JwtUtil jwtUtil;

    /**
     * 초대 토큰을 검증하고 InviteEntity를 반환한다.
     * @param inviteToken 초대 토큰
     * @param oauthEmail  OAuth2 로그인한 사용자 이메일
     * @return 유효한 InviteEntity
     */
    public InviteEntity validate(String inviteToken, String oauthEmail) {
        // 1. JWT 검증
        try {
            jwtUtil.validateToken(inviteToken);
        } catch (Exception e) {
            log.error(" InviteToken 유효하지 않음", e);
            throw toAuthException(InviteErrorCode.INVALID_TOKEN);
        }

        // 2. DB 조회
        InviteEntity invite = inviteRepository.findByInviteTokenAndStatus(
                inviteToken, InviteEntity.InviteStatus.PENDING
        ).orElseThrow(() -> toAuthException(InviteErrorCode.INVALID_TOKEN));

        // 3. 만료 여부 확인
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            invite.inviteExpired();
            log.error(" 초대 토큰 만료: {}", inviteToken);
            throw toAuthException(InviteErrorCode.EXPIRED_TOKEN);
        }

        // 4. 이메일 일치 여부 확인
        if (!invite.getEmail().equalsIgnoreCase(oauthEmail)) {
            log.error(" 초대 이메일 불일치: invite={}, oauth2={}", invite.getEmail(), oauthEmail);
            throw toAuthException(InviteErrorCode.EMAIL_MISMATCH);
        }

        return invite;
    }

    /**
     * InviteErrorCode → OAuth2AuthenticationException 변환
     */
    private OAuth2AuthenticationException toAuthException(InviteErrorCode errorCode) {
        return new OAuth2AuthenticationException(
                new OAuth2Error(errorCode.getCode(), errorCode.getMessage(), null),
                errorCode.getMessage(),
                new InviteException(errorCode) // cause 로 원본 예외 포함
        );
    }
}
