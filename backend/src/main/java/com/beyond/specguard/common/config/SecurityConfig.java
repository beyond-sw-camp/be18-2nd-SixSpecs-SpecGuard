package com.beyond.specguard.common.config;

import com.beyond.specguard.auth.model.filter.JwtFilter;
import com.beyond.specguard.auth.model.filter.LoginFilter;
import com.beyond.specguard.auth.model.handler.CustomFailureHandler;
import com.beyond.specguard.auth.model.handler.CustomSuccessHandler;
import com.beyond.specguard.auth.model.repository.ClientUserRepository;
import com.beyond.specguard.auth.model.service.RedisTokenService;
import com.beyond.specguard.common.jwt.JwtUtil;
import com.beyond.specguard.common.exception.RestAccessDeniedHandler;
import com.beyond.specguard.common.exception.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtUtil jwtUtil;
    private final ClientUserRepository clientUserRepository;
    private final CustomSuccessHandler customSuccessHandler;
    private final CustomFailureHandler customFailureHandler;
    private final RedisTokenService redisTokenService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint; //  주입
    private final RestAccessDeniedHandler restAccessDeniedHandler;           //  주입

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/api/v1/auth/signup/**",
                        "/api/v1/auth/login",
                        "/api/v1/auth/token/refresh",
                        "/api/v1/invite/accept/**"
                ).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/**").hasAnyRole("OWNER", "MANAGER", "VIEWER")
                .requestMatchers("/api/v1/invite/**").hasRole("OWNER")
                .anyRequest().authenticated()
        );

        // 🔹 인증/인가 실패 핸들러 - 스프링 빈 주입된 걸 사용
        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint(restAuthenticationEntryPoint)   // 401
                .accessDeniedHandler(restAccessDeniedHandler)            // 403
        );

        http.addFilterBefore(
                new JwtFilter(jwtUtil, clientUserRepository, redisTokenService, restAuthenticationEntryPoint),
                UsernamePasswordAuthenticationFilter.class
        );

        LoginFilter loginFilter = new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil);
        loginFilter.setAuthenticationSuccessHandler(customSuccessHandler);
        loginFilter.setAuthenticationFailureHandler(customFailureHandler);
        http.addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173")); // ✅ 프론트 주소
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // ✅ 쿠키 허용
        config.setExposedHeaders(List.of("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}