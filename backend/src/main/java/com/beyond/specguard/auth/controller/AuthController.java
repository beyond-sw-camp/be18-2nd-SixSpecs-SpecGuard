package com.beyond.specguard.auth.controller;

import com.beyond.specguard.auth.dto.ReissueResponseDTO;
import com.beyond.specguard.auth.dto.RefreshRequestDTO;
import com.beyond.specguard.auth.dto.SignupRequestDTO;
import com.beyond.specguard.auth.dto.SignupResponseDTO;
import com.beyond.specguard.auth.service.ReissueService;
import com.beyond.specguard.auth.service.SignupService;
import com.beyond.specguard.common.jwt.JwtUtil;
import com.beyond.specguard.common.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SignupService signupService;
    private final ReissueService reissueService;
    private final JwtUtil jwtUtil;

    @PostMapping("/signup/user")
    public ResponseEntity<SignupResponseDTO> signup(@Valid @RequestBody SignupRequestDTO request) {
        SignupResponseDTO response = signupService.signup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<ReissueResponseDTO> reissue(
            @RequestBody RefreshRequestDTO request,
            HttpServletResponse response
    ) {
        ReissueResponseDTO dto = reissueService.reissue(request.getRefreshToken());

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
}
