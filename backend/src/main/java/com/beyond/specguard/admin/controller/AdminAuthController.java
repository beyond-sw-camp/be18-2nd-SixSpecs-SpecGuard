package com.beyond.specguard.admin.controller;

import com.beyond.specguard.admin.model.dto.InternalAdminRequestDto;
import com.beyond.specguard.admin.model.entity.InternalAdmin;
import com.beyond.specguard.admin.model.service.InternalAdminService;
import com.beyond.specguard.auth.model.service.LogoutService;
import com.beyond.specguard.auth.model.service.ReissueService;
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


}
