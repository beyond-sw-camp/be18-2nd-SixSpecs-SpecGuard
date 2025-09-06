package com.beyond.specguard.auth.model.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public CustomOAuth2AuthorizationRequestResolver(ClientRegistrationRepository repo) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request);
        return customizeRequest(req, request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request, clientRegistrationId);
        return customizeRequest(req, request);
    }

    private OAuth2AuthorizationRequest customizeRequest(OAuth2AuthorizationRequest req, HttpServletRequest request) {
        if (req == null) return null;

        String inviteToken = request.getParameter("inviteToken");
        if (inviteToken == null || inviteToken.isBlank()) {
            log.debug("➡️ inviteToken 없음: state 그대로 사용");
            return req;
        }

        // ✅ 파이프(|) 대신 "__" 사용 (URL-safe)
        String state = req.getState() + "__" + inviteToken;

        log.info("➡️ OAuth2 요청에 state 값 세팅: {}", state);

        return OAuth2AuthorizationRequest.from(req)
                .state(state)
                .build();
    }
}
