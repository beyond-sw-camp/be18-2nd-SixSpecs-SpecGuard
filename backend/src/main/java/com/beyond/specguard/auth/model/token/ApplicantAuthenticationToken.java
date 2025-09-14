package com.beyond.specguard.auth.model.token;


import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class ApplicantAuthenticationToken extends UsernamePasswordAuthenticationToken {

    public ApplicantAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }

    public ApplicantAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }
}
