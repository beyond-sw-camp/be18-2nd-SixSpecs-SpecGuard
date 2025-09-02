package com.beyond.specguard.auth.controller;

import com.beyond.specguard.auth.model.dto.*;
import com.beyond.specguard.auth.model.entity.ClientUser;
import com.beyond.specguard.auth.model.service.InviteSignupService;
import com.beyond.specguard.auth.model.service.LogoutService;
import com.beyond.specguard.auth.model.service.ReissueService;
import com.beyond.specguard.auth.model.service.SignupService;
import com.beyond.specguard.common.jwt.JwtUtil;
import com.beyond.specguard.common.util.CookieUtil;
import com.beyond.specguard.common.util.TokenResponseWriter;
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
    private final InviteSignupService inviteSignupService;
    private final TokenResponseWriter tokenResponseWriter;


    @PostMapping("/signup/company")
    public ResponseEntity<SignupResponseDto> signup(@Valid @RequestBody SignupRequestDto request) {
        SignupResponseDto response = signupService.signup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<Void> reissue(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = CookieUtil.getCookieValue(request, "refresh_token");
        ReissueResponseDto dto= reissueService.reissue(refreshToken);
        tokenResponseWriter.writeTokens(response, dto);
        return ResponseEntity.ok().build();

    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
        logoutService.logout(request,response);
        return ResponseEntity.ok(Map.of("message", "로그아웃이 정상적으로 처리되었습니다."));
    }

    @PostMapping("/signup/invite")
    public ResponseEntity<SignupResponseDto> signupWithInvite(
            @RequestBody @Valid InviteSignupRequestDto request
    ) {
        SignupResponseDto response = inviteSignupService.signupWithInvite(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup/invite/check")
    public ResponseEntity<InviteCheckResponseDto> checkInvite(
            @RequestBody @Valid InviteCheckRequestDto request
    ) {
        InviteCheckResponseDto response = inviteSignupService.checkInvite(request.getToken());
        return ResponseEntity.ok(response);
    }
}
