package com.beyond.specguard.auth.service;

import com.beyond.specguard.auth.entity.ClientUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final ClientUser user;

    public CustomUserDetails(ClientUser user) {
        this.user = user;
    }

    public String getId() {
        return user.getId().toString(); // UUID → String 변환
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security 권한은 "ROLE_" prefix 필수
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        // 로그인은 email 기반
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // 👉 필요 시 서비스 계층에서 ClientUser 전체를 꺼낼 수 있도록 getter 추가
    public ClientUser getUser() {
        return user;
    }
}
