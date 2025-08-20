package com.beyond.specguard.auth.service;

import com.beyond.specguard.auth.dto.ReissueResponseDTO;
import com.beyond.specguard.auth.entity.RefreshEntity;
import com.beyond.specguard.auth.repository.RefreshRepository;
import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.common.exception.errorcode.AuthErrorCode;
import com.beyond.specguard.common.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReissueService {

    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    @Transactional
    public ReissueResponseDTO reissue(String refreshToken) {
        log.info("🔁 [ReissueService] 리프레시 요청 처리");

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new CustomException(AuthErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        if (!"refresh".equals(jwtUtil.getCategory(refreshToken))) {
            throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        if (!refreshRepository.existsByRefresh(refreshToken)) {
            throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        String username = jwtUtil.getUsername(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        String newAccess = jwtUtil.createAccessToken(username, role);
        String newRefresh = jwtUtil.createRefreshToken(username, role);

        refreshRepository.deleteByRefresh(refreshToken);
        saveRefreshEntity(username, newRefresh);

        return ReissueResponseDTO.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .message("access & refresh 토큰 재발급 성공")
                .build();
    }

    private void saveRefreshEntity(String username, String refresh) {
        Date expiration = jwtUtil.getExpiration(refresh);
        RefreshEntity entity = RefreshEntity.builder()
                .username(username)
                .refresh(refresh)
                .expiration(expiration)
                .build();
        refreshRepository.save(entity);
    }
}
