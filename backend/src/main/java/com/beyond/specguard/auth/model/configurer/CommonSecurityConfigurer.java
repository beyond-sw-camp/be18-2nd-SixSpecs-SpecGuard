package com.beyond.specguard.auth.model.configurer;

import com.beyond.specguard.admin.model.repository.InternalAdminRepository;
import com.beyond.specguard.auth.model.filter.JwtFilter;
import com.beyond.specguard.auth.model.service.RedisTokenService;
import com.beyond.specguard.client.model.repository.ClientUserRepository;
import com.beyond.specguard.common.exception.RestAccessDeniedHandler;
import com.beyond.specguard.common.exception.RestAuthenticationEntryPoint;
import com.beyond.specguard.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// JWT 필터 등록, 예외 처리, 공통 필터
@Configuration
@RequiredArgsConstructor
public class CommonSecurityConfigurer extends AbstractHttpConfigurer<CommonSecurityConfigurer, HttpSecurity> {

    private final JwtUtil jwtUtil;
    private final RedisTokenService redisTokenService;
    private final ClientUserRepository clientUserRepository;
    private final InternalAdminRepository internalAdminRepository;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                // 🔹 인증/인가 실패 핸들러
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint) // 401
                        .accessDeniedHandler(restAccessDeniedHandler)   // 403
                )
                // 🔹 JWT 필터
                .addFilterBefore(
                        new JwtFilter(jwtUtil, clientUserRepository, redisTokenService, restAuthenticationEntryPoint, internalAdminRepository),
                        UsernamePasswordAuthenticationFilter.class
                );
    }


}
