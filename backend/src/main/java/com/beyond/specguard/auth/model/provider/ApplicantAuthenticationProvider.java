package com.beyond.specguard.auth.model.provider;

import com.beyond.specguard.applicant.model.service.ApplicantDetailsService;
import com.beyond.specguard.auth.model.token.ApplicantAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

public class ApplicantAuthenticationProvider extends DaoAuthenticationProvider {
    public ApplicantAuthenticationProvider(
            ApplicantDetailsService applicantDetailService,
            PasswordEncoder passwordEncoder
    ) {
        super(applicantDetailService);
        setPasswordEncoder(passwordEncoder);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ApplicantAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
