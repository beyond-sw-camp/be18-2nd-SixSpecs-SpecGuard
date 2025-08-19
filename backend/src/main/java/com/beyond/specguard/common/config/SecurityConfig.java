package com.beyond.specguard.common.config;

import com.beyond.specguard.auth.filter.JwtFilter;
import com.beyond.specguard.auth.filter.LoginFilter;
import com.beyond.specguard.auth.handler.CustomFailureHandler;
import com.beyond.specguard.auth.handler.CustomSuccessHandler;
import com.beyond.specguard.auth.repository.RefreshRepository;
import com.beyond.specguard.auth.repository.UserRepository;
import com.beyond.specguard.common.jwt.JwtUtil;
import com.beyond.specguard.common.exception.RestAccessDeniedHandler;
import com.beyond.specguard.common.exception.RestAuthenticationEntryPoint;
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
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;
    private final UserRepository userRepository;
    private final CustomSuccessHandler customSuccessHandler;
    private final CustomFailureHandler customFailureHandler;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration,
                          JwtUtil jwtUtil,
                          RefreshRepository refreshRepository,
                          UserRepository userRepository,
                          CustomSuccessHandler customSuccessHandler,
                          CustomFailureHandler customFailureHandler) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
        this.userRepository = userRepository;
        this.customSuccessHandler = customSuccessHandler;
        this.customFailureHandler = customFailureHandler;
    }

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
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 인가 규칙
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/signup/**", "/api/v1/auth/login").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/**").hasAnyRole("APPLICANT", "ADMIN")  // 🔥 여기 수정
                .anyRequest().authenticated()
        );


        // 인증/인가 실패 핸들러
        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                .accessDeniedHandler(new RestAccessDeniedHandler())
        );

        // JWT 인증 필터 등록 (UsernamePasswordAuthenticationFilter 전에 실행)
        http.addFilterBefore(new JwtFilter(jwtUtil, userRepository), UsernamePasswordAuthenticationFilter.class);

        // 로그인 필터 등록 (폼 로그인 대신 동작)
        LoginFilter loginFilter = new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, refreshRepository);
        loginFilter.setAuthenticationSuccessHandler(customSuccessHandler);
        loginFilter.setAuthenticationFailureHandler(customFailureHandler);
        http.addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 프론트엔드 연동을 위한 CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:5173")); // TODO: 환경변수로 분리 권장
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("access", "Set-Cookie"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
