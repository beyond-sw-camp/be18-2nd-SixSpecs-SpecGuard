package com.beyond.specguard.auth.model.token;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

// Client 인증용 토큰
public class ClientAuthenticationToken extends UsernamePasswordAuthenticationToken {
    public ClientAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }

    public ClientAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }
}
