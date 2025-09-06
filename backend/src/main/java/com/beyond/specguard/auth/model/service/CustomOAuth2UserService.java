package com.beyond.specguard.auth.model.service;

import com.beyond.specguard.auth.model.dto.response.GoogleResponseDto;
import com.beyond.specguard.auth.model.dto.response.NaverResponseDto;
import com.beyond.specguard.auth.model.dto.response.OAuth2Response;
import com.beyond.specguard.auth.model.entity.ClientUser;
import com.beyond.specguard.auth.model.repository.ClientUserRepository;
import com.beyond.specguard.common.jwt.JwtUtil;
import com.beyond.specguard.invite.exception.InviteException;
import com.beyond.specguard.invite.exception.errorcode.InviteErrorCode;
import com.beyond.specguard.invite.model.entity.InviteEntity;
import com.beyond.specguard.invite.model.repository.InviteRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final ClientUserRepository clientUserRepository;
    private final InviteRepository inviteRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("🌐 Provider: {}", registrationId);

        // ✅ Provider별 파싱
        OAuth2Response oAuth2Response;
        if ("google".equals(registrationId)) {
            oAuth2Response = new GoogleResponseDto(oAuth2User.getAttributes());
        } else if ("naver".equals(registrationId)) {
            oAuth2Response = new NaverResponseDto(oAuth2User.getAttributes());
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 Provider: " + registrationId);
        }

        String email = oAuth2Response.getEmail();
        String provider = oAuth2Response.getProvider();
        String providerId = oAuth2Response.getProviderId();
        String name = oAuth2Response.getName();

        log.info("👤 OAuth2 사용자 정보: email={}, name={}, provider={}", email, name, provider);

        // ✅ 기존 유저 확인
        ClientUser user = clientUserRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // ✅ HttpServletRequest에서 state 꺼내기
            HttpServletRequest servletRequest =
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String state = servletRequest.getParameter("state");
            log.info("📥 콜백 state 값: {}", state);

            String inviteToken = extractInviteToken(state);
            log.info("📥 추출된 inviteToken: {}", inviteToken);

            try {
                jwtUtil.validateToken(inviteToken); // ✅ 서명 및 만료 검증
            } catch (Exception e) {
                log.error("❌ InviteToken 유효하지 않음", e);
                throw new InviteException(InviteErrorCode.INVALID_TOKEN);
            }

            // ✅ JWT claim 추출
            String inviteEmail = jwtUtil.getInviteEmail(inviteToken);
            String slug = jwtUtil.getInviteSlug(inviteToken);
            String role = jwtUtil.getRole(inviteToken);

            log.info("📜 InviteToken Claims: email={}, slug={}, role={}", inviteEmail, slug, role);

            InviteEntity invite = inviteRepository.findByInviteTokenAndStatus(
                    inviteToken, InviteEntity.InviteStatus.PENDING
            ).orElseThrow(() -> new InviteException(InviteErrorCode.INVALID_TOKEN));

            if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
                invite.inviteExpired();
                log.error("❌ 초대 토큰 만료: {}", inviteToken);
                throw new InviteException(InviteErrorCode.EXPIRED_TOKEN);
            }

            if (!invite.getEmail().equalsIgnoreCase(email)) {
                log.error("❌ 초대 이메일 불일치: invite={}, oauth2={}", invite.getEmail(), email);
                throw new InviteException(InviteErrorCode.FORBIDDEN_INVITE);
            }

            // ✅ 신규 유저 생성
            user = ClientUser.builder()
                    .company(invite.getCompany())
                    .email(email)
                    .name(name)
                    .role(ClientUser.Role.valueOf(role)) // ✅ 토큰에서 뽑은 role 사용
                    .provider(provider)
                    .providerId(providerId)
                    .profileImage(
                            (oAuth2Response instanceof NaverResponseDto naverResp) ? naverResp.getProfileImage() : null
                    )
                    .build();

            String companyName = invite.getCompany().getName();

            clientUserRepository.save(user);
            invite.inviteAccepted();

            log.info("✅ 신규 소셜 유저 가입 성공: email={}, company={}", email, companyName);
        } else {
            log.info("✅ 기존 유저 로그인: email={}", user.getEmail());
        }

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    private String extractInviteToken(String state) {
        if (state == null) {
            log.warn("⚠️ state 값이 null임!");
            return null;
        }
        if (state.contains("__")) {
            return state.split("__")[1]; // ✅ "__" 기준으로 분리
        }
        return state;
    }
}
