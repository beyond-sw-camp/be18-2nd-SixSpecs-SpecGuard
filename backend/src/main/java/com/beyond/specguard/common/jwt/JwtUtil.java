package com.beyond.specguard.common.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private final SecretKey secretKey;

    // ✅ TTL (밀리초 단위)
    private static final long ACCESS_TTL = 3 * 60 * 1000L;            // 15분
    private static final long REFRESH_TTL = 14L * 24 * 60 * 60 * 1000; // 14일
    private static final long INVITE_TTL = 7L * 24 * 60 * 60 * 1000;   // 7일

    public JwtUtil(@Value("${spring.jwt.secret}") String secret) {
        this.secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );
    }

    // ================== Access Token ==================
    public String createAccessToken(String username, String role, String companySlug) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString()) // ✅ jti
                .claim("category", "access")
                .claim("username", username)
                .claim("role", role)
                .claim("companySlug", companySlug)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TTL))
                .signWith(secretKey)
                .compact();
    }

    // ================== Refresh Token ==================
    public String createRefreshToken(String username) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString()) // ✅ jti
                .claim("category", "refresh")
                .claim("username", username)      // ✅ 최소한의 식별자만
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TTL))
                .signWith(secretKey)
                .compact();
    }

    // ================== Invite 토큰 ==================
    public String createInviteToken(String email, String slug, String role) {
        return Jwts.builder()
                .claim("category", "invite")
                .claim("email", email)
                .claim("slug", slug)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + INVITE_TTL))
                .signWith(secretKey)
                .compact();
    }

    // ================== Claim 추출 ==================
    public String getUsername(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload()
                .get("username", String.class);
    }

    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload()
                .get("role", String.class);
    }

    public String getCategory(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload()
                .get("category", String.class);
    }

    public String getCompanySlug(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload()
                .get("companySlug", String.class);
    }

    // Invite 전용 claim 추출
    public String getInviteEmail(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload()
                .get("email", String.class);
    }

    public String getInviteSlug(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload()
                .get("slug", String.class);
    }

    // ================== jti 추출 ==================
    public String getJti(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload()
                .getId();
    }

    // ================== Expiration ==================
    public boolean isExpired(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload()
                .getExpiration()
                .before(new Date());
    }

    public Date getExpiration(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload()
                .getExpiration();
    }

    public void validateToken(String token) throws ExpiredJwtException {
        Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
        //  여기서 만료되면 ExpiredJwtException 발생
    }
}
