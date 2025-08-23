package com.beyond.specguard.auth.controller;

import com.beyond.specguard.auth.dto.ReissueResponseDTO;
import com.beyond.specguard.auth.dto.RefreshRequestDTO;
import com.beyond.specguard.auth.dto.SignupRequestDTO;
import com.beyond.specguard.auth.dto.SignupResponseDTO;
import com.beyond.specguard.auth.service.LogoutService;
import com.beyond.specguard.auth.service.ReissueService;
import com.beyond.specguard.auth.service.SignupService;
import com.beyond.specguard.common.jwt.JwtUtil;
import com.beyond.specguard.common.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// api 명세서 수정이 필요합니다 지금 api경로 그대로 사용할껀지 하니면 수정 할껀지 고려해봐야 할 것 같습니다
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SignupService signupService;
    private final ReissueService reissueService;
    private final JwtUtil jwtUtil;
    private final LogoutService logoutService;

    @PostMapping("/signup/company")
    public ResponseEntity<SignupResponseDTO> signup(@Valid @RequestBody SignupRequestDTO request) {
        SignupResponseDTO response = signupService.signup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<ReissueResponseDTO> reissue(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // ✅ 쿠키에서 refresh_token 꺼내기
        String refreshToken = CookieUtil.getCookieValue(request, "refresh_token");

        // ✅ Service 호출
        ReissueResponseDTO dto = reissueService.reissue(refreshToken);

        // ✅ 새 Access Token → Authorization 헤더
        response.setHeader("Authorization", "Bearer " + dto.getAccessToken());

        // ✅ 새 Refresh Token → HttpOnly 쿠키
        int maxAge = (int) (
                (jwtUtil.getExpiration(dto.getRefreshToken()).getTime() - System.currentTimeMillis()) / 1000
        );

        response.addCookie(
                CookieUtil.createHttpOnlyCookie(
                        "refresh_token",
                        dto.getRefreshToken(),
                        maxAge
                )
        );

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        logoutService.logout(request);
        return ResponseEntity.ok(Map.of("message", "로그아웃이 정상적으로 처리되었습니다."));
    }

}
