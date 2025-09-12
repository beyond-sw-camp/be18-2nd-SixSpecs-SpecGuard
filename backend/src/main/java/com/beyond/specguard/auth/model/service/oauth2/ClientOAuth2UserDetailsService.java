package com.beyond.specguard.auth.model.service.oauth2;

import com.beyond.specguard.auth.model.dto.response.GoogleResponseDto;
import com.beyond.specguard.auth.model.dto.response.NaverResponseDto;
import com.beyond.specguard.auth.model.dto.response.OAuth2Response;
import com.beyond.specguard.auth.model.entity.ClientUser;
import com.beyond.specguard.auth.model.repository.ClientUserRepository;
import com.beyond.specguard.common.util.JwtUtil;
import com.beyond.specguard.common.util.OAuth2StateUtil;
import com.beyond.specguard.invite.model.entity.InviteEntity;
import com.beyond.specguard.invite.model.service.InviteValidator;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientOAuth2UserDetailsService extends DefaultOAuth2UserService {

    private final ClientUserRepository clientUserRepository;
    private final JwtUtil jwtUtil;
    private final InviteValidator inviteValidator;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("Provider: {}", registrationId);

        // 1. Provider별 사용자 정보 파싱
        OAuth2Response oAuth2Response = switch (registrationId) {
            case "google" -> new GoogleResponseDto(oAuth2User.getAttributes());
            case "naver" -> new NaverResponseDto(oAuth2User.getAttributes());
            default -> throw new OAuth2AuthenticationException("지원하지 않는 Provider: " + registrationId);
        };

        String email = oAuth2Response.getEmail();
        String provider = oAuth2Response.getProvider();
        String providerId = oAuth2Response.getProviderId();
        String name = oAuth2Response.getName();

        log.info("OAuth2 사용자 정보: email={}, name={}, provider={}", email, name, provider);

        // 2. 기존 사용자 확인
        ClientUser user = clientUserRepository.findByEmail(email)
                .orElseGet(() -> createNewUserFromInvite(oAuth2Response, email, provider, providerId, name));

        if (user.getId() != null) {
            log.info("기존 유저 로그인: email={}", user.getEmail());
        }

        return new ClientOAuth2UserDetails(user, oAuth2User.getAttributes());
    }

    /**
     * 초대 토큰을 검증하고 신규 유저를 생성한다.
     */
    private ClientUser createNewUserFromInvite(OAuth2Response oAuth2Response,
                                               String email,
                                               String provider,
                                               String providerId,
                                               String name) {

        HttpServletRequest servletRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String state = servletRequest.getParameter("state");
        log.info("콜백 state 값: {}", state);

        // 초대 토큰 추출
        String inviteToken = OAuth2StateUtil.extractInviteToken(state);
        log.info("추출된 inviteToken: {}", inviteToken);

        // 초대 검증
        InviteEntity invite = inviteValidator.validate(inviteToken, email);

        // 신규 유저 생성
        ClientUser newUser = ClientUser.builder()
                .company(invite.getCompany())
                .email(email)
                .name(name)
                .role(ClientUser.Role.valueOf(jwtUtil.getRole(inviteToken)))
                .provider(provider)
                .providerId(providerId)
                .profileImage(
                        (oAuth2Response instanceof NaverResponseDto naverResp)
                                ? naverResp.getProfileImage() : null
                )
                .build();

        clientUserRepository.save(newUser);
        invite.inviteAccepted();

        log.info("신규 소셜 유저 가입 성공: email={}, company={}", email, invite.getCompany().getName());

        return newUser;
    }
}
