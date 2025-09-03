package com.beyond.specguard.auth.model.service;

import com.beyond.specguard.auth.model.dto.ReissueResponseDto;
import com.beyond.specguard.auth.model.entity.ClientUser;
import com.beyond.specguard.auth.model.repository.ClientUserRepository;
import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.auth.exception.errorcode.AuthErrorCode;
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

    @Transactional
    public ReissueResponseDto reissue(String refreshToken) {
        log.info("🔁 [ReissueService] 리프레시 요청 처리");

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        //  RefreshToken 만료 검사
        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new CustomException(AuthErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        //  category 확인
        if (!"refresh".equals(jwtUtil.getCategory(refreshToken))) {
            throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        //  username 추출
        String username = jwtUtil.getUsername(refreshToken);

        //  Redis에서 RefreshToken 확인
        String savedRefresh = redisTokenService.getRefreshToken(username);
        if (savedRefresh == null || !savedRefresh.equals(refreshToken)) {
            throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        //  DB에서 유저 다시 조회 → role, slug 확보
        ClientUser user = userRepository.findByEmailWithCompany(username)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        String role = user.getRole().name();
        String companySlug = user.getCompany().getSlug();

        //  새 토큰 발급
        String newAccess = jwtUtil.createAccessToken(username, role, companySlug);
        String newRefresh = jwtUtil.createRefreshToken(username);

        //  Redis 갱신
        redisTokenService.deleteRefreshToken(username);
        long ttl = (jwtUtil.getExpiration(newRefresh).getTime() - System.currentTimeMillis()) / 1000;
        redisTokenService.saveRefreshToken(username, newRefresh, ttl);

        return ReissueResponseDto.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .message("access & refresh 토큰 재발급 성공")
                .build();
    }
}
