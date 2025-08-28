//package com.beyond.specguard.verification.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//
//public class SecurityConfig {
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable()) // API 테스트 편의상 CSRF 비활성화
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/verify/phone/**").permitAll() // 인증 없이 허용
//                        .anyRequest().permitAll() // 일단 전체 오픈 (추후 tighten)
//                )
//                .httpBasic(httpBasic -> httpBasic.disable()); // 기본 인증 팝업도 꺼버리기
//
//        return http.build();
//    }
//}
