package com.beyond.specguard.auth.model.filter;

import com.beyond.specguard.auth.model.dto.request.LoginRequestDto;
import com.beyond.specguard.common.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ClientLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager clientAuthenticationManager;
    private final JwtUtil jwtUtil;

    public ClientLoginFilter(@Qualifier("clientAuthenticationManager") AuthenticationManager clientAuthenticationManager, JwtUtil jwtUtil) {
        super.setAuthenticationManager(clientAuthenticationManager);
        this.clientAuthenticationManager = clientAuthenticationManager;
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/api/v1/auth/login"); // 로그인 엔드포인트
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            LoginRequestDto loginDTO = objectMapper.readValue(request.getInputStream(), LoginRequestDto.class);

            UsernamePasswordAuthenticationToken authToken =
                    new ClientAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword());

            return this.getAuthenticationManager().authenticate(authToken);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
