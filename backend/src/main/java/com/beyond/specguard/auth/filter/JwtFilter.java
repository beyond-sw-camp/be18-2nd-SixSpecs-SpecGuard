package com.beyond.specguard.auth.filter;

import com.beyond.specguard.auth.repository.UserRepository;
import com.beyond.specguard.common.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        // ✅ Authorization 헤더가 없거나 Bearer 로 시작하지 않으면 그냥 통과
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);

        try {
            // ✅ 토큰 만료 여부
            if (jwtUtil.isExpired(token)) {
                throw new ExpiredJwtException(null, null, "Access token expired");
            }

            // ✅ 카테고리 확인 (access 토큰만 허용)
            String category = jwtUtil.getCategory(token);
            if (!"access".equals(category)) {
                throw new BadCredentialsException("Invalid token category");
            }

            // ✅ 사용자 정보 추출
            String username = jwtUtil.getUsername(token);
            String role = jwtUtil.getRole(token);

            // ✅ DB 사용자 존재 여부 확인
            if (!userRepository.existsByEmail(username)) {
                throw new BadCredentialsException("User not found");
            }

            // ✅ 인증 객체 생성
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
            );

            // ✅ SecurityContext 에 저장
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            // ❌ 여기서 응답 처리하지 않음
            // Spring Security가 RestAuthenticationEntryPoint 를 통해 JSON 응답 반환하도록 위임
            SecurityContextHolder.clearContext();
            throw e; // 예외를 던져서 entryPoint 로 위임
        }

        // ✅ 다음 필터 실행
        filterChain.doFilter(request, response);
    }
}
