package com.beyond.specguard.auth.dto;

import java.util.Map;

@SuppressWarnings("unchecked")
public class NaverResponseDTO implements OAuth2Response {

    private final Map<String, Object> attributes;
    private final Map<String, Object> response;

    public NaverResponseDTO(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.response = (Map<String, Object>) attributes.get("response");
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getProviderId() {
        return response.get("id").toString(); // 네이버 고유 ID
    }

    @Override
    public String getEmail() {
        return (String) response.get("email"); // null 가능
    }

    @Override
    public String getName() {
        return (String) response.get("name"); // null 가능
    }

    // 필요하다면 프로필 이미지도 꺼낼 수 있음
    public String getProfileImage() {
        return (String) response.get("profile_image");
    }
}
