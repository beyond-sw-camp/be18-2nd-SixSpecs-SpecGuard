package com.beyond.specguard.common.config;

import com.beyond.specguard.admin.model.repository.InternalAdminRepository;
import com.beyond.specguard.auth.model.filter.AdminLoginFilter;
import com.beyond.specguard.auth.model.filter.ClientLoginFilter;
import com.beyond.specguard.auth.model.filter.JwtFilter;
import com.beyond.specguard.auth.model.handler.CustomFailureHandler;
import com.beyond.specguard.auth.model.handler.CustomSuccessHandler;
import com.beyond.specguard.auth.model.provider.AdminAuthenticationProvider;
import com.beyond.specguard.auth.model.provider.ClientAuthenticationProvider;
import com.beyond.specguard.auth.model.repository.ClientUserRepository;
import com.beyond.specguard.auth.model.service.RedisTokenService;
import com.beyond.specguard.common.exception.RestAccessDeniedHandler;
import com.beyond.specguard.common.exception.RestAuthenticationEntryPoint;
import com.beyond.specguard.common.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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

    private final static String[] CLIENT_AUTH_WHITE_LIST = {
            // SpringDocs OpenApi Swagger API
            "/swagger-ui/**", "/v3/api-docs/**",
            "/api/v1/auth/signup/**",
            "/api/v1/auth/login",
            "/api/v1/auth/token/refresh",
            "/api/v1/invite/accept/**",
            "/api/v1/auth/invite/**"
    };

    private final static String[] ADMIN_AUTH_WHITE_LIST = {
            // SpringDocs OpenApi Swagger API
            "/swagger-ui/**", "/v3/api-docs/**",
            "/api/v1/auth/token/refresh",
            "/admins/auth/login"
    };

    @Bean("adminAuthenticationManager")
    @Primary
    public AuthenticationManager adminAuthenticationManager(
            AdminAuthenticationProvider adminAuthenticationProvider
    ) {
        return new ProviderManager(adminAuthenticationProvider);
    }

    @Bean("clientAuthenticationManager")
    public AuthenticationManager clientAuthenticationManager(
            ClientAuthenticationProvider clientAuthenticationProvider
    ) {
        return new ProviderManager(clientAuthenticationProvider);
    }


    /**
     * Admin 전용 SecurityFilterChain
     */
    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http,
                                                        @Qualifier("adminAuthenticationManager") AuthenticationManager adminAuthenticationManager) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http
                .securityMatcher("/admins/**") // Admin 전용 엔드포인트만 적용
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(ADMIN_AUTH_WHITE_LIST).permitAll()
                        .anyRequest().hasRole("ADMIN")
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint) // Admin 전용 EntryPoint
                        .accessDeniedHandler(restAccessDeniedHandler) // Admin 전용 AccessDenied
                );

        http
                .addFilterBefore(
                        new JwtFilter(jwtUtil, clientUserRepository, redisTokenService, restAuthenticationEntryPoint, internalAdminRepository),
                        UsernamePasswordAuthenticationFilter.class
                );

        AdminLoginFilter adminLoginFilter = new AdminLoginFilter(adminAuthenticationManager);
        adminLoginFilter.setAuthenticationSuccessHandler(customSuccessHandler);
        adminLoginFilter.setAuthenticationFailureHandler(customFailureHandler);

        http.addFilterAt(adminLoginFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Client 전용 SecurityFilterChain
     */
    @Bean
    @Order(2)
    public SecurityFilterChain clientSecurityFilterChain(HttpSecurity http,
                                                         @Qualifier("clientAuthenticationManager") AuthenticationManager clientAuthenticationManager
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http
                .securityMatcher("/api/v1/**") // Client 전용 엔드포인트만 적용
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(CLIENT_AUTH_WHITE_LIST).permitAll()
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
                new JwtFilter(jwtUtil, clientUserRepository, redisTokenService, restAuthenticationEntryPoint, internalAdminRepository),
                UsernamePasswordAuthenticationFilter.class
        );

        ClientLoginFilter clientLoginFilter = new ClientLoginFilter(clientAuthenticationManager, jwtUtil);
        clientLoginFilter.setAuthenticationSuccessHandler(customSuccessHandler);
        clientLoginFilter.setAuthenticationFailureHandler(customFailureHandler);

        http.addFilterAt(clientLoginFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
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