package com.beyond.specguard.common.config;

import com.beyond.specguard.auth.model.filter.JwtFilter;
import com.beyond.specguard.auth.model.filter.LoginFilter;
import com.beyond.specguard.auth.model.handler.CustomFailureHandler;
import com.beyond.specguard.auth.model.handler.CustomSuccessHandler;
import com.beyond.specguard.auth.model.repository.ClientUserRepository;
import com.beyond.specguard.auth.model.service.RedisTokenService;
import com.beyond.specguard.common.exception.RestAccessDeniedHandler;
import com.beyond.specguard.common.exception.RestAuthenticationEntryPoint;
import com.beyond.specguard.common.jwt.JwtUtil;
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
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtUtil jwtUtil;
    private final ClientUserRepository clientUserRepository;
    private final InternalAdminRepository internalAdminRepository;
    private final CustomSuccessHandler customSuccessHandler;
    private final CustomFailureHandler customFailureHandler;
    private final RedisTokenService redisTokenService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final OAuth2AuthorizationRequestResolver customResolver;

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
            "/api/v1/auth"
    };

    private final static String[] ADMIN_AUTH_WHITE_LIST = {
            // SpringDocs OpenApi Swagger API
            "/swagger-ui/**", "/v3/api-docs/**",
            "/admins/auth/login",
            "/admins/auth/token/refresh"
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
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 🔹 요청 인가 설정
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(AUTH_WHITE_LIST).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/invite/**").hasRole("OWNER")
                .requestMatchers("/api/**").hasAnyRole("OWNER", "MANAGER", "VIEWER")
                .anyRequest().authenticated()
        );

        // 🔹 인증/인가 실패 핸들러
        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint(restAuthenticationEntryPoint)   // 401
                .accessDeniedHandler(restAccessDeniedHandler)            // 403
        );

        // 🔹 JWT 필터
        http.addFilterBefore(
                new JwtFilter(jwtUtil, clientUserRepository, redisTokenService, restAuthenticationEntryPoint),
                UsernamePasswordAuthenticationFilter.class
        );

        ClientLoginFilter clientLoginFilter = new ClientLoginFilter(clientAuthenticationManager, jwtUtil);
        clientLoginFilter.setAuthenticationSuccessHandler(customSuccessHandler);
        clientLoginFilter.setAuthenticationFailureHandler(customFailureHandler);

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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173")); // 프론트 주소
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // 쿠키 허용
        config.setExposedHeaders(List.of("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}