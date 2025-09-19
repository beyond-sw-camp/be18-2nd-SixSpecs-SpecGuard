package com.beyond.specguard.admin.model.service;

import com.beyond.specguard.admin.model.entity.InternalAdmin;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class InternalAdminDetails implements UserDetails {

    private final InternalAdmin admin;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Role 이름에 ROLE_ 접두사 붙이기
        return List.of(new SimpleGrantedAuthority("ROLE_" + admin.getRole().name()));
    }

    @Override
    public String getPassword() {
        return admin.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return admin.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
