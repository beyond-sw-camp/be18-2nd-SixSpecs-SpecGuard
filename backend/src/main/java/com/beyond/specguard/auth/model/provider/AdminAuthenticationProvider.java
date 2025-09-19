package com.beyond.specguard.auth.model.provider;

import com.beyond.specguard.admin.model.service.InternalAdminDetailService;
import com.beyond.specguard.auth.model.token.AdminAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminAuthenticationProvider extends DaoAuthenticationProvider {

    public AdminAuthenticationProvider(
            InternalAdminDetailService internalAdminDetailService,
            PasswordEncoder passwordEncoder
    ) {
        super(internalAdminDetailService);
        super.setPasswordEncoder(passwordEncoder);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return AdminAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
