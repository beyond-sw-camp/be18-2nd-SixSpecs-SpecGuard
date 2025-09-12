package com.beyond.specguard.auth.model.filter;

import com.beyond.specguard.applicant.model.dto.ApplicantLoginRequestDto;
import com.beyond.specguard.auth.model.token.ApplicantAuthenticationToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

public class ApplicantLoginFilter extends UsernamePasswordAuthenticationFilter {
    public ApplicantLoginFilter(@Qualifier("applicantAuthenticationManager") AuthenticationManager applicantAuthenticationManager) {
        super(applicantAuthenticationManager);
        setFilterProcessesUrl("/api/v1/resumes/login");
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            ApplicantLoginRequestDto loginRequestDto =  new ObjectMapper().readValue(request.getInputStream(), ApplicantLoginRequestDto.class);
            ApplicantAuthenticationToken authToken =
                    new ApplicantAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword());
            return this.getAuthenticationManager().authenticate(authToken);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
