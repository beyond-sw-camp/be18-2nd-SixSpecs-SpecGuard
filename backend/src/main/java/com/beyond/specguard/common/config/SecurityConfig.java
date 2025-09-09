package com.beyond.specguard.common.config;

import com.beyond.specguard.admin.model.repository.InternalAdminRepository;
import com.beyond.specguard.admin.model.service.InternalAdminDetailService;
import com.beyond.specguard.auth.model.filter.JwtFilter;
import com.beyond.specguard.auth.model.filter.LoginFilter;
import com.beyond.specguard.auth.model.handler.CustomFailureHandler;
import com.beyond.specguard.auth.model.handler.CustomSuccessHandler;
import com.beyond.specguard.auth.model.repository.ClientUserRepository;
import com.beyond.specguard.auth.model.service.CustomUserDetailsService;
import com.beyond.specguard.auth.model.service.RedisTokenService;
import com.beyond.specguard.common.exception.RestAccessDeniedHandler;
import com.beyond.specguard.common.exception.RestAuthenticationEntryPoint;
import com.beyond.specguard.common.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
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

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final ClientUserRepository clientUserRepository;
    private final InternalAdminRepository internalAdminRepository;
    private final CustomSuccessHandler customSuccessHandler;
    private final CustomFailureHandler customFailureHandler;
    private final RedisTokenService redisTokenService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint; //  주입
    private final RestAccessDeniedHandler restAccessDeniedHandler;           //  주입

    private final static String[] AUTH_WHITE_LIST = {
            // SpringDocs OpenApi Swagger API
            "/swagger-ui/**", "/v3/api-docs/**",
            "/api/v1/auth/signup/**",
            "/api/v1/auth/login",
            "/api/v1/auth/token/refresh",
            "/api/v1/invite/accept/**",
            "/api/v1/auth/invite/**"
    };

    @Bean
    public AuthenticationProvider adminAuthenticationProvider(InternalAdminDetailService adminUserDetailsService,
                                                              PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(adminUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationProvider clientAuthenticationProvider(CustomUserDetailsService clientUserDetailsService,
                                                               PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(clientUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http,
            AuthenticationProvider adminAuthenticationProvider,
            AuthenticationProvider clientAuthenticationProvider
            ) throws Exception {

        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(adminAuthenticationProvider)
                .authenticationProvider(clientAuthenticationProvider)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, InternalAdminDetailService adminUserDetailsService, CustomUserDetailsService customUserDetailsService) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(AUTH_WHITE_LIST).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/**").hasAnyRole("OWNER", "MANAGER", "VIEWER", "ADMIN")
                .requestMatchers("/api/v1/invite/**").hasRole("OWNER")
                .anyRequest().permitAll()
        );

        // 🔹 인증/인가 실패 핸들러 - 스프링 빈 주입된 걸 사용
        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint(restAuthenticationEntryPoint)   // 401
                .accessDeniedHandler(restAccessDeniedHandler)            // 403
        );

        http.addFilterBefore(
                new JwtFilter(jwtUtil, clientUserRepository, redisTokenService, restAuthenticationEntryPoint, internalAdminRepository),
                UsernamePasswordAuthenticationFilter.class
        );

        LoginFilter loginFilter = new LoginFilter(authenticationManager(http, adminAuthenticationProvider(adminUserDetailsService, passwordEncoder()), clientAuthenticationProvider(customUserDetailsService, passwordEncoder())
        ),
                jwtUtil);
        loginFilter.setAuthenticationSuccessHandler(customSuccessHandler);
        loginFilter.setAuthenticationFailureHandler(customFailureHandler);
        http.addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173")); //  프론트 주소
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); //  쿠키 허용
        config.setExposedHeaders(List.of("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}