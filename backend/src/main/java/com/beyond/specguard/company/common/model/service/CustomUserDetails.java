package com.beyond.specguard.company.common.model.service;

import com.beyond.specguard.company.common.model.entity.ClientCompany;
import com.beyond.specguard.company.common.model.entity.ClientUser;
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

    //  유저 엔티티 접근
    public ClientUser getUser() {
        return user;
    }

    //  회사 엔티티 바로 접근 (헬퍼 메서드)
    public ClientCompany getCompany() {
        return user.getCompany();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
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
}
