package com.beyond.specguard.admin.controller;

import com.beyond.specguard.admin.model.dto.request.InternalAdminRequestDto;
import com.beyond.specguard.admin.model.dto.response.InternalAdminResponseDto;
import com.beyond.specguard.admin.model.service.InternalAdminService;
import com.beyond.specguard.auth.model.dto.response.ReissueResponseDto;
import com.beyond.specguard.auth.model.service.LogoutService;
import com.beyond.specguard.auth.model.service.ReissueService;
import com.beyond.specguard.common.util.CookieUtil;
import com.beyond.specguard.common.util.TokenResponseWriter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
@Tag(name = "Internal Admin", description = "Internal Admin 계정 관련 API")
public class AdminAuthController {
    private final InternalAdminService internalAdminService;
    private final LogoutService logoutService;
    private final ReissueService reissueService;
    private final TokenResponseWriter tokenResponseWriter;

    @Operation(
            summary = "Internal Admin 생성",
            description = "새로운 Internal Admin 계정을 생성합니다."
    )
    @PostMapping("/create")
    public ResponseEntity<InternalAdminResponseDto> createAdmin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Internal Admin 생성 요청 DTO",
                    required = true,
                    content = @Content(schema = @Schema(implementation = InternalAdminRequestDto.class))
            )
            @Valid @RequestBody InternalAdminRequestDto request
    ) {
        InternalAdminResponseDto admin = internalAdminService.createAdmin(request);
        return ResponseEntity.ok(admin);
    }


    @Operation(
            summary = "Internal Admin 로그아웃",
            description = "관리자 계정을 로그아웃 처리하고, 액세스 토큰을 블랙리스트에 등록합니다."
    )
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @Parameter(description = "Authorization 헤더에 Bearer 토큰을 포함", required = true)
            HttpServletRequest request,
            HttpServletResponse response
    ) {
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
