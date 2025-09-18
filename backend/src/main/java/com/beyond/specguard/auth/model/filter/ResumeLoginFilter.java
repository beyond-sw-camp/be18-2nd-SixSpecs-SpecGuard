package com.beyond.specguard.auth.model.filter;

import com.beyond.specguard.auth.model.handler.local.CustomFailureHandler;
import com.beyond.specguard.auth.model.token.ApplicantAuthenticationToken;
import com.beyond.specguard.resume.model.dto.request.ResumeLoginRequestDto;
import com.beyond.specguard.resume.model.service.ResumeDetails;
import com.beyond.specguard.resume.model.service.ResumeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

public class ResumeLoginFilter extends UsernamePasswordAuthenticationFilter {
    private final ResumeService resumeService;

    public ResumeLoginFilter(
            @Qualifier("applicantAuthenticationManager") AuthenticationManager applicantAuthenticationManager,
            CustomFailureHandler customFailureHandler,
            ResumeService resumeService
    ) {
        super(applicantAuthenticationManager);
        setAuthenticationFailureHandler(customFailureHandler);
        this.resumeService = resumeService;
        setFilterProcessesUrl("/api/v1/resumes/login");
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            ResumeLoginRequestDto loginRequestDto =  new ObjectMapper().readValue(request.getInputStream(), ResumeLoginRequestDto.class);
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
        response.setContentType("application/json"); // JSON 타입 설정
        response.setCharacterEncoding("UTF-8");

        ResumeDetails resumeDetails = ((ResumeDetails) authResult.getPrincipal());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mapper.writeValue(response.getWriter(), resumeService.loginAndGetResume(resumeDetails.getResume().getId()));
    }
}
