package com.beyond.specguard.admin.controller;

import com.beyond.specguard.admin.model.dto.InternalAdminRequestDto;
import com.beyond.specguard.admin.model.entity.InternalAdmin;
import com.beyond.specguard.admin.model.service.InternalAdminService;
import com.beyond.specguard.auth.model.dto.response.ReissueResponseDto;
import com.beyond.specguard.auth.model.service.LogoutService;
import com.beyond.specguard.auth.model.service.ReissueService;
import com.beyond.specguard.common.util.CookieUtil;
import com.beyond.specguard.common.util.TokenResponseWriter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admins/auth")
@RequiredArgsConstructor
public class AdminAuthController {
    private final InternalAdminService internalAdminService;
    private final LogoutService logoutService;
    private final ReissueService reissueService;
    private final TokenResponseWriter tokenResponseWriter;

    @PostMapping("/create")
    public ResponseEntity<InternalAdmin> createAdmin(
            @RequestBody InternalAdminRequestDto request
    ) {
        InternalAdmin admin = internalAdminService.createAdmin(request);
        return ResponseEntity.ok(admin);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
        logoutService.logout(request,response);
        return ResponseEntity.ok(Map.of("message", "로그아웃이 정상적으로 처리되었습니다."));
    }

    @Operation(
            summary = "Access Token 갱신",
            description = "쿠키에 담긴 Refresh Token을 이용해 Access Token을 갱신합니다.",
            security = {
                    @SecurityRequirement(name = "refreshTokenCookie")
            }
    )
    @PostMapping("/token/refresh")
    public ResponseEntity<Void> reissue(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = CookieUtil.getCookieValue(request, "refresh_token");

        ReissueResponseDto dto= reissueService.reissue(true, refreshToken);
        tokenResponseWriter.writeTokens(response, dto);
        return ResponseEntity.ok().build();

    }

}
