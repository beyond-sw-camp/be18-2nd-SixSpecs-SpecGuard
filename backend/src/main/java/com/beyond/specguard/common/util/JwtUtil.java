<<<<<<< HEAD:backend/src/main/java/com/beyond/specguard/common/jwt/JwtUtil.java
package com.beyond.specguard.common.jwt;

import com.beyond.specguard.common.properties.AppProperties;
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
    private final AppProperties appProperties;

    public JwtUtil(@Value("${spring.jwt.secret}") String secret,
                   AppProperties appProperties) {
        this.secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );
        this.appProperties = appProperties;
    }

    // ================== Access Token ==================
    public String createAccessToken(String username, String role, String companySlug) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .claim("category", "access")
                .claim("username", username)
                .claim("role", role)
                .claim("companySlug", companySlug)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + appProperties.getJwt().getAccessTtl()))
                .signWith(secretKey)
                .compact();
    }

    // ================== Refresh Token ==================
    public String createRefreshToken(String username) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .claim("category", "refresh")
                .claim("username", username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + appProperties.getJwt().getRefreshTtl()))
                .signWith(secretKey)
                .compact();
    }

    // ================== Invite Token ==================
    public String createInviteToken(String email, String slug, String role) {
        return Jwts.builder()
                .claim("category", "invite")
                .claim("email", email)
                .claim("slug", slug)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + appProperties.getJwt().getInviteTtl()))
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
    }
}
=======
package com.beyond.specguard.common.util;

import com.beyond.specguard.common.properties.AppProperties;
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
    private final AppProperties appProperties;

    public JwtUtil(@Value("${spring.jwt.secret}") String secret,
                   AppProperties appProperties) {
        this.secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );
        this.appProperties = appProperties;
    }

    // ================== Access Token ==================
    public String createAccessToken(String username, String role, String companySlug) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .claim("category", "access")
                .claim("username", username)
                .claim("role", role)
                .claim("companySlug", companySlug)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + appProperties.getJwt().getAccessTtl()))
                .signWith(secretKey)
                .compact();
    }

    // ================== Refresh Token ==================
    public String createRefreshToken(String username) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .claim("category", "refresh")
                .claim("username", username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + appProperties.getJwt().getRefreshTtl()))
                .signWith(secretKey)
                .compact();
    }

    // ================== Invite Token ==================
    public String createInviteToken(String email, String slug, String role) {
        return Jwts.builder()
                .claim("category", "invite")
                .claim("email", email)
                .claim("slug", slug)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + appProperties.getJwt().getInviteTtl()))
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
    }
}
>>>>>>> develop:backend/src/main/java/com/beyond/specguard/common/util/JwtUtil.java
