package com.beyond.specguard.auth.model.service;

import com.beyond.specguard.admin.model.entity.InternalAdmin;
import com.beyond.specguard.admin.model.repository.InternalAdminRepository;
import com.beyond.specguard.auth.exception.errorcode.AuthErrorCode;
import com.beyond.specguard.auth.model.dto.response.ReissueResponseDto;
import com.beyond.specguard.auth.model.entity.ClientUser;
import com.beyond.specguard.auth.model.repository.ClientUserRepository;
import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.common.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReissueService {

    private final JwtUtil jwtUtil;
    private final RedisTokenService redisTokenService;
    private final ClientUserRepository userRepository;
    private final InternalAdminRepository internalAdminRepository;

    @Transactional
    public ReissueResponseDto reissue(boolean isAdmin, String refreshToken) {
        log.info("🔁 [ReissueService] 리프레시 요청 처리");

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        //  RefreshToken 만료 검사
        try {
            jwtUtil.validateToken(refreshToken); // ExpiredJwtException 던짐
        } catch (ExpiredJwtException e) {
            throw new CustomException(AuthErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        //  category 확인
        if (!"refresh".equals(jwtUtil.getCategory(refreshToken))) {
            throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        //  username 추출
        String username = jwtUtil.getUsername(refreshToken);

        //  Redis 에서 RefreshToken 확인
        String savedRefresh = redisTokenService.getRefreshToken(username);
        if (savedRefresh == null || !savedRefresh.equals(refreshToken)) {
            throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        String role;
        String companySlug;
        if (!isAdmin) {
        //  DB에서 유저 다시 조회 → role, slug 확보
            ClientUser user = userRepository.findByEmailWithCompany(username)
                    .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

            role = user.getRole().name();
            companySlug = user.getCompany().getSlug();

        } else {
            InternalAdmin admin = internalAdminRepository.findByEmail(username)
                    .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

            role = admin.getRole().name();
            companySlug = null;

        }

        //  새 토큰 발급
        String newAccess = jwtUtil.createAccessToken(username, role, companySlug);
        String newRefresh = jwtUtil.createRefreshToken(username);

        //  Redis 갱신
        redisTokenService.deleteRefreshToken(username);
        long refreshTtl = (jwtUtil.getExpiration(newRefresh).getTime() - System.currentTimeMillis()) / 1000;
        redisTokenService.saveRefreshToken(username, newRefresh, refreshTtl);

        //  세션 갱신 (새 AccessToken jti 기준, refresh TTL 유지)
        String newAccessJti = jwtUtil.getJti(newAccess);
        redisTokenService.saveUserSession(username, newAccessJti, refreshTtl);

        return ReissueResponseDto.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .message("access & refresh 토큰 재발급 성공")
                .build();
    }
}