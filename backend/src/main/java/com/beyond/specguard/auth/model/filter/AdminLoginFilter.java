package com.beyond.specguard.auth.model.filter;

import com.beyond.specguard.auth.model.dto.request.LoginRequestDto;
import com.beyond.specguard.auth.model.handler.CustomFailureHandler;
import com.beyond.specguard.auth.model.handler.CustomSuccessHandler;
import com.beyond.specguard.auth.model.token.AdminAuthenticationToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

public class AdminLoginFilter extends UsernamePasswordAuthenticationFilter {

    public AdminLoginFilter(
            @Qualifier("adminAuthenticationManager") AuthenticationManager adminAuthenticationManager,
            CustomSuccessHandler customSuccessHandler,
            CustomFailureHandler customFailureHandler
    ) {
        super.setAuthenticationManager(adminAuthenticationManager);
        setAuthenticationSuccessHandler(customSuccessHandler);
        setAuthenticationFailureHandler(customFailureHandler);
        setFilterProcessesUrl("/admins/auth/login"); // 어드민 전용 로그인 엔드포인트
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws AuthenticationException {
        try {
            LoginRequestDto loginDTO = new ObjectMapper().readValue(request.getInputStream(), LoginRequestDto.class);
            AdminAuthenticationToken authToken =
                    new AdminAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword());
            return this.getAuthenticationManager().authenticate(authToken);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
