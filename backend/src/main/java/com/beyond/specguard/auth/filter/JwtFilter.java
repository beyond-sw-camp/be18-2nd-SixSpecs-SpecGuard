package com.beyond.specguard.auth.filter;

import com.beyond.specguard.auth.entity.ClientUser;
import com.beyond.specguard.auth.repository.ClientUserRepository;
import com.beyond.specguard.auth.service.CustomUserDetails;
import com.beyond.specguard.auth.service.RedisTokenService;
import com.beyond.specguard.common.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ClientUserRepository clientUserRepository;
    private final RedisTokenService redisTokenService; // ✅ 추가

    public JwtFilter(JwtUtil jwtUtil,
                     ClientUserRepository clientUserRepository,
                     RedisTokenService redisTokenService) {
        this.jwtUtil = jwtUtil;
        this.clientUserRepository = clientUserRepository;
        this.redisTokenService = redisTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);

        try {
            if (jwtUtil.isExpired(token)) {
                throw new BadCredentialsException("Invalid or expired token");
            }

            String jti = jwtUtil.getJti(token);
            if (redisTokenService.isBlacklisted(jti)) {
                throw new BadCredentialsException("Invalid or expired token");
            }

            String category = jwtUtil.getCategory(token);
            if (!"access".equals(category)) {
                throw new BadCredentialsException("Invalid or expired token");
            }

            String email = jwtUtil.getUsername(token);

            ClientUser user = clientUserRepository.findByEmailWithCompany(email)
                    .orElseThrow(() -> new BadCredentialsException("Invalid or expired token"));

            CustomUserDetails userDetails = new CustomUserDetails(user);
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            throw new BadCredentialsException("Invalid or expired token");
        }
    }
}
