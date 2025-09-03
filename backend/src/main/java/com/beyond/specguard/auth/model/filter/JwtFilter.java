package com.beyond.specguard.auth.model.filter;

import com.beyond.specguard.auth.model.entity.ClientUser;
import com.beyond.specguard.auth.model.repository.ClientUserRepository;
import com.beyond.specguard.auth.model.service.CustomUserDetails;
import com.beyond.specguard.auth.model.service.RedisTokenService;
import com.beyond.specguard.auth.exception.AuthException;
import com.beyond.specguard.auth.exception.errorcode.AuthErrorCode;
import com.beyond.specguard.common.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ClientUserRepository clientUserRepository;
    private final RedisTokenService redisTokenService;
    private final AuthenticationEntryPoint entryPoint; // ✅ 주입받음

    public JwtFilter(JwtUtil jwtUtil,
                     ClientUserRepository clientUserRepository,
                     RedisTokenService redisTokenService,
                     AuthenticationEntryPoint entryPoint) {
        this.jwtUtil = jwtUtil;
        this.clientUserRepository = clientUserRepository;
        this.redisTokenService = redisTokenService;
        this.entryPoint = entryPoint;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/login")
                || path.startsWith("/api/v1/auth/signup")
                || path.startsWith("/api/v1/auth/token/refresh");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        System.out.println(">>> JwtFilter 진입, path=" + request.getRequestURI());
        String authorization = request.getHeader("Authorization");
        System.out.println(">>> Authorization=" + authorization);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);

        try {
            // ✅ 만료 여부 검증
            jwtUtil.validateToken(token);

            // 블랙리스트 확인
            String jti = jwtUtil.getJti(token);
            if (redisTokenService.isBlacklisted(jti)) {
                throw new AuthException(AuthErrorCode.BLACKLISTED_ACCESS_TOKEN);
            }

            // 카테고리 확인
            String category = jwtUtil.getCategory(token);
            if (!"access".equals(category)) {
                throw new AuthException(AuthErrorCode.INVALID_TOKEN_CATEGORY);
            }

            // 사용자 조회
            String email = jwtUtil.getUsername(token);
            ClientUser user = clientUserRepository.findByEmailWithCompany(email)
                    .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

            CustomUserDetails userDetails = new CustomUserDetails(user);
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            SecurityContextHolder.clearContext();
            System.out.println(">>> ExpiredJwtException 잡힘, ACCESS_TOKEN_EXPIRED로 변환해서 EntryPoint 호출");
            entryPoint.commence(request, response,
                    new AuthException(AuthErrorCode.ACCESS_TOKEN_EXPIRED));
            return;
        } catch (AuthException e) {
            SecurityContextHolder.clearContext();
            System.out.println(">>> AuthException 잡힘, code=" + e.getErrorCode().getCode());
            entryPoint.commence(request, response, e);
            return;
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            System.out.println(">>> Exception 잡힘, UNAUTHORIZED로 변환: " + e.getClass());
            entryPoint.commence(request, response,
                    new AuthException(AuthErrorCode.UNAUTHORIZED));
            return;
        }

    }
}
