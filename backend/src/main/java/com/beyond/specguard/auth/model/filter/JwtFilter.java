package com.beyond.specguard.auth.model.filter;

import com.beyond.specguard.auth.model.entity.ClientUser;
import com.beyond.specguard.auth.model.repository.ClientUserRepository;
import com.beyond.specguard.auth.model.service.CustomUserDetails;
import com.beyond.specguard.auth.model.service.RedisTokenService;
import com.beyond.specguard.common.exception.AuthException;
import com.beyond.specguard.common.exception.errorcode.AuthErrorCode;
import com.beyond.specguard.common.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ClientUserRepository clientUserRepository;
    private final RedisTokenService redisTokenService;

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

        // Authorization 헤더가 없거나 Bearer 로 시작하지 않으면 그냥 통과
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);

        try {
            // 토큰 만료 여부 확인
            if (jwtUtil.isExpired(token)) {
                throw new AuthException(AuthErrorCode.ACCESS_TOKEN_EXPIRED);
            }

            // 블랙리스트 확인
            String jti = jwtUtil.getJti(token);
            if (redisTokenService.isBlacklisted(jti)) {
                throw new AuthException(AuthErrorCode.BLACKLISTED_ACCESS_TOKEN);
            }

            // 카테고리 확인 (access 토큰만 허용)
            String category = jwtUtil.getCategory(token);
            if (!"access".equals(category)) {
                throw new AuthException(AuthErrorCode.INVALID_TOKEN_CATEGORY);
            }

            // 사용자 정보 추출
            String email = jwtUtil.getUsername(token);

            ClientUser user = clientUserRepository.findByEmailWithCompany(email)
                    .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

            // CustomUserDetails 생성
            CustomUserDetails userDetails = new CustomUserDetails(user);

            // 인증 객체 생성
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            // SecurityContext에 저장
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext();
            throw e; // Security 필터 체인에서 처리됨
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            throw new AuthException(AuthErrorCode.UNAUTHORIZED);
        }

        // 다음 필터 실행
        filterChain.doFilter(request, response);
    }
}
