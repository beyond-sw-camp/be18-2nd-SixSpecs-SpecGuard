package com.beyond.specguard.common.config;

import com.beyond.specguard.auth.model.configurer.CommonSecurityConfigurer;
import com.beyond.specguard.auth.model.filter.AdminLoginFilter;
import com.beyond.specguard.auth.model.filter.ClientLoginFilter;
import com.beyond.specguard.auth.model.filter.ResumeLoginFilter;
import com.beyond.specguard.auth.model.handler.local.CustomFailureHandler;
import com.beyond.specguard.auth.model.handler.local.CustomSuccessHandler;
import com.beyond.specguard.auth.model.handler.oauth2.OAuth2FailureHandler;
import com.beyond.specguard.auth.model.handler.oauth2.OAuth2SuccessHandler;
import com.beyond.specguard.auth.model.provider.AdminAuthenticationProvider;
import com.beyond.specguard.auth.model.provider.ClientAuthenticationProvider;
import com.beyond.specguard.auth.model.provider.ResumeAuthenticationProvider;
import com.beyond.specguard.common.exception.RestAccessDeniedHandler;
import com.beyond.specguard.common.exception.RestAuthenticationEntryPoint;
import com.beyond.specguard.resume.model.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    // Login Handlers
    private final CustomSuccessHandler customSuccessHandler;
    private final CustomFailureHandler customFailureHandler;

    // OAuth2
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final OAuth2AuthorizationRequestResolver customResolver;

    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    private final static String[] AUTH_WHITE_LIST = {
            // Swagger
            "/swagger-ui/**", "/v3/api-docs/**",

            // Auth API
            "/api/v1/auth/signup/**",
            "/api/v1/auth/login",
            "/api/v1/auth/token/refresh",
            "/api/v1/auth/token/**",
            "/api/v1/auth/invite/**",

            // Invite API
            "/api/v1/invite/accept/**",

            // OAuth2 관련 엔드포인트
            "/oauth2/authorization/**",
            "/login/oauth2/code/**",
            "/api/v1/auth",

            //resume
            "/api/v1/resumes/**"
    };

    private final static String[] ADMIN_AUTH_WHITE_LIST = {
            // SpringDocs OpenApi Swagger API
            "/swagger-ui/**", "/v3/api-docs/**",
            "/admins/auth/login",
            "/admins/auth/token/refresh"
    };

    private static final String[] APPLICANT_AUTH_WHITE_LIST = {
            "/api/v1/resumes/login",
            "/api/v1/resumes/**"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

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

    @Bean("resumeAuthenticationManager")
    public AuthenticationManager resumeAuthenticationManager(
            ResumeAuthenticationProvider resumeAuthenticationProvider
    ) {
        return new ProviderManager(resumeAuthenticationProvider);
    }

    /**
     * Admin 전용 SecurityFilterChain
     */
    @Bean
    @Order(2)
    public SecurityFilterChain adminSecurityFilterChain(
            HttpSecurity http,
            @Qualifier("adminAuthenticationManager") AuthenticationManager adminAuthenticationManager,
            CommonSecurityConfigurer configurer
    ) throws Exception {
        // 전역 세팅
        applyGlobalSettings(http);

        // 공통 예외처리, 필터 설정
        http.with(configurer, Customizer.withDefaults());


        // Admin 전용 엔드포인트만 적용
        http.securityMatcher("/admins/**")
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(ADMIN_AUTH_WHITE_LIST).permitAll()
                    .anyRequest().hasRole("ADMIN")
            );

        AdminLoginFilter adminLoginFilter = new AdminLoginFilter(adminAuthenticationManager, customSuccessHandler, customFailureHandler);

        http.addFilterAt(adminLoginFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Client 전용 SecurityFilterChain
     */
    @Bean
    @Order(3)
    public SecurityFilterChain clientSecurityFilterChain(
            HttpSecurity http,
            @Qualifier("clientAuthenticationManager") AuthenticationManager clientAuthenticationManager,
            CommonSecurityConfigurer configurer
    ) throws Exception {
        // 전역 세팅
        applyGlobalSettings(http);

        // 공통 설정
        http.with(configurer, Customizer.withDefaults());

        // 🔹 요청 인가 설정
        http.securityMatcher("/api/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(AUTH_WHITE_LIST).permitAll()
                .requestMatchers("/api/v1/invite/**").hasRole("OWNER")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/company/**").hasRole("OWNER")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/company/**").hasRole("OWNER")
                .requestMatchers("/api/**").hasAnyRole("OWNER", "MANAGER", "VIEWER")
                .anyRequest().authenticated()
        );

        ClientLoginFilter clientLoginFilter = new ClientLoginFilter(clientAuthenticationManager, customSuccessHandler, customFailureHandler);

        http.addFilterAt(clientLoginFilter, UsernamePasswordAuthenticationFilter.class);

        // 🔹 OAuth2 로그인
        http.oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authEndpoint -> authEndpoint
                        .authorizationRequestResolver(customResolver) // ✅ 커스텀 Resolver 등록
                )
                .successHandler(oAuth2SuccessHandler) // ✅ 성공 핸들러
                .failureHandler(oAuth2FailureHandler) // ✅ 실패 핸들러
        );

        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain resumeSecurityFilterChain(
            HttpSecurity http,
            @Qualifier("resumeAuthenticationManager") AuthenticationManager resumeAuthenticationManager,
            ResumeService resumeService) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(true)
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // Applicant 전용 엔드포인트만 적용
        http.securityMatcher("/api/v1/resumes/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(APPLICANT_AUTH_WHITE_LIST).permitAll()
                        .anyRequest().hasRole("APPLICANT")
                );
        http
                .exceptionHandling(ex -> ex
                .authenticationEntryPoint(restAuthenticationEntryPoint) // 401
                .accessDeniedHandler(restAccessDeniedHandler)   // 403
                );

        ResumeLoginFilter loginFilter = new ResumeLoginFilter(resumeAuthenticationManager, customFailureHandler, resumeService);
        http.addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // Jwt 기반의 http 빌딩을 함수화
    private void applyGlobalSettings(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(cors -> cors.configurationSource(corsConfigurationSource()));
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000")); // 프론트 주소
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // 쿠키 허용
        config.setExposedHeaders(List.of("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}