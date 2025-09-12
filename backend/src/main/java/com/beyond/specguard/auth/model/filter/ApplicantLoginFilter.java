package com.beyond.specguard.auth.model.filter;

import com.beyond.specguard.applicant.model.dto.ApplicantLoginRequestDto;
import com.beyond.specguard.auth.model.handler.local.CustomFailureHandler;
import com.beyond.specguard.auth.model.token.ApplicantAuthenticationToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

public class ApplicantLoginFilter extends UsernamePasswordAuthenticationFilter {
    public ApplicantLoginFilter(
            @Qualifier("applicantAuthenticationManager") AuthenticationManager applicantAuthenticationManager,
            CustomFailureHandler customFailureHandler
    ) {
        super(applicantAuthenticationManager);
        setAuthenticationFailureHandler(customFailureHandler);
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

            authToken.setDetails(loginRequestDto);

            return this.getAuthenticationManager().authenticate(authToken);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult) throws IOException, ServletException {

        // ✅ 필수
        // SecurityContextHolder에 인증 객체 저장
        SecurityContextHolder.getContext().setAuthentication(authResult);

        HttpSession session = request.getSession(true);

        // ✅ 필수
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("로그인 성공");
    }
}
