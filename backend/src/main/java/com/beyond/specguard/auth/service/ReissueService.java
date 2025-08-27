package com.beyond.specguard.auth.service;

import com.beyond.specguard.auth.dto.ReissueResponseDto;
import com.beyond.specguard.auth.entity.ClientUser;
import com.beyond.specguard.auth.entity.RefreshEntity;
import com.beyond.specguard.auth.repository.ClientUserRepository;
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
    private final ClientUserRepository userRepository; // ✅ 추가

    @Transactional
    public ReissueResponseDto reissue(String refreshToken) {
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

        // ✅ 토큰에서 username/role 꺼내기
        String username = jwtUtil.getUsername(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        // ✅ DB에서 유저 다시 조회 → slug 추출
        ClientUser user = userRepository.findByEmail(username)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));
        String companySlug = user.getCompany().getSlug();

        // ✅ 새로운 토큰 생성 (slug 포함)
        String newAccess = jwtUtil.createAccessToken(username, role, companySlug);
        String newRefresh = jwtUtil.createRefreshToken(username, role, companySlug);

        // ✅ 기존 refresh 제거 후 새 refresh 저장
        refreshRepository.deleteByRefresh(refreshToken);
        saveRefreshEntity(username, newRefresh);

        return ReissueResponseDto.builder()
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
