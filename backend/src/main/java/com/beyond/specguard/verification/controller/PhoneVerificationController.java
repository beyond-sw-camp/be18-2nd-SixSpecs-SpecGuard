package com.beyond.specguard.verification.controller;


import com.beyond.specguard.verification.dto.VerifyDto;
import com.beyond.specguard.verification.service.PhoneVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/verify/phone")
@RequiredArgsConstructor
public class PhoneVerificationController {
    private final PhoneVerificationService service;

    @PostMapping("/start")
    public VerifyDto.VerifyStartResponse start(@Valid @RequestBody VerifyDto.VerifyStartRequest req,
                                               Authentication auth) {
        String userId = null;
        // 추후 실제 로그인 붙이면 여기서 principal 타입 체크해서 꺼내면 됨
        // if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) { ... }
        return service.start(req, userId);
    }

    // 폴링 (UI에서 2~3초 간격으로 호출)
    @GetMapping("/poll")
    public VerifyDto.VerifyPollResponse poll(@RequestParam String tid) {
        return service.poll(tid);
    }


    @PostMapping("/finish")
    public Map<String, String> finish(@Valid @RequestBody VerifyDto.VerifyFinishRequest req) {


        service.finish(req.tid(), req.token(), req.phone());
        return Map.of("status", "SUCCESS");
    }


}