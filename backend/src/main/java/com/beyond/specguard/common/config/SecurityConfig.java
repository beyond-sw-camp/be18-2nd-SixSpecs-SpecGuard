package com.beyond.specguard.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

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

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/signup/user").permitAll() // 회원가입만 허용
                .requestMatchers("/api/verify/**").permitAll() // 휴대폰 인증 허용 (테스트)
                .anyRequest().permitAll() // 일단 전체 오픈 (추후 tighten)

//                .anyRequest().authenticated()
        );
//                .httpBasic(httpBasic -> httpBasic.disable()); // 기본 인증 팝업도 꺼버리기;

        return http.build();
    }

}
