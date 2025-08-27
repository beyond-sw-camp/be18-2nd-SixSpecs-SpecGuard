package com.beyond.specguard.company.invite.controller;

import com.beyond.specguard.company.invite.dto.InviteRequestDto;
import com.beyond.specguard.company.invite.dto.InviteResponseDto;
import com.beyond.specguard.company.invite.service.InviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

//지금은 restcontroller로 두고 뷰 반환이 될때
@RestController
@RequestMapping("/api/v1/company/{slug}/invite")
@RequiredArgsConstructor
public class InviteController {

    private final InviteService inviteService;

    // ✅ 초대 생성 (OWNER만 자기 회사 직원 초대 가능)
    @PostMapping
    public ResponseEntity<InviteResponseDto> sendInvite(
            @PathVariable String slug,
            @AuthenticationPrincipal com.beyond.specguard.auth.service.CustomUserDetails currentUser,
            @RequestBody @Validated InviteRequestDto request
    ) {
        InviteResponseDto response = inviteService.sendInvite(slug, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

//    // ✅ 초대 수락 (로그인 안 한 사용자도 접근 가능)
//    @GetMapping("/accept")
//    public ResponseEntity<String> acceptInvite(@RequestParam String token){
//        inviteService.acceptInvite(token);
//        return ResponseEntity.ok("초대가 수락되었습니다.");
//    }
}
