package com.beyond.specguard.auth.model.token;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

// Admin 인증용 토큰
public class AdminAuthenticationToken extends UsernamePasswordAuthenticationToken {
    public AdminAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }

    public AdminAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }
}
