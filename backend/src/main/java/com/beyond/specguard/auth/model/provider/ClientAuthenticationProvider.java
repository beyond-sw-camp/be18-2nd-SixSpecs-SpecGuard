package com.beyond.specguard.auth.model.provider;

import com.beyond.specguard.client.model.service.local.ClientUserDetailsService;
import com.beyond.specguard.auth.model.token.ClientAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class ClientAuthenticationProvider extends DaoAuthenticationProvider {

    public ClientAuthenticationProvider(
            ClientUserDetailsService clientUserDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        super(clientUserDetailsService);
        super.setPasswordEncoder(passwordEncoder);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ClientAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
