package com.beyond.specguard.verification.controller;


import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.common.exception.errorcode.VerifyErrorCode;
import com.beyond.specguard.verification.dto.VerifyDto;
import com.beyond.specguard.verification.service.PhoneVerificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/verify/phone")
@RequiredArgsConstructor
public class PhoneVerificationController {
    private final PhoneVerificationService service;

    @PostMapping("/start")
    public VerifyDto.VerifyStartResponse start(@Valid @RequestBody VerifyDto.VerifyStartRequest req,
                                                Authentication auth) {
        String userId = null;
        return service.start(req, userId);
    }

    // 폴링 (UI에서 2~3초 간격으로 호출)
    @GetMapping("/poll")
    public VerifyDto.VerifyPollResponse poll(@RequestParam ("token") String token) {
        return service.poll(token);
    }

    @PostMapping("/finish")
    public Map<String, String> finish(@Valid @RequestBody VerifyDto.VerifyFinishRequest req) {


        service.finish(req.token(), req.phone());
        return Map.of("status", "SUCCESS");
    }

    // SMS 게이트웨이/메일 웹훅이 JSON으로 POST
    @PostMapping(consumes = "application/json")
    public ResponseEntity<Void> inbound(
            @RequestBody @Valid InboundPayload p,
            @RequestHeader(value = "X-Verify-Secret", required = false) String secret // 보안용(옵션)
    ) {
        // (권장) 웹훅 보안 검사: shared secret/HMAC 등
        // if (!"your-secret".equals(secret)) return ResponseEntity.status(403).build();

        final String token = extractSixDigits(p.body());
        final String from  = p.from(); // 전화번호 또는 이메일

        // 1) SMS 게이트웨이(전화번호가 from 에 옴) → phone+token으로 검증
        if (from != null && from.matches(".*[0-9].*")) {
            service.verify(from, token); // service.verify 내부에서 normalize 수행
            return ResponseEntity.accepted().build();
        }

        // 2) 이메일(iMessage/메일) 웹훅처럼 'from'이 전화번호가 아닌 경우
        //    → 토큰으로 Redis에서 phone 찾아서 검증 (※ 보안상 보낼 단말 번호 확인은 불가)
        service.finish(token, null); // or service.verifyByTokenOnly(token) 별도 메서드로 분리해도 됨
        return ResponseEntity.accepted().build();
    }

    private String extractSixDigits(String body) {
        if (body == null) throw new CustomException(VerifyErrorCode.INVALID_OTP_CODE);
        Matcher m = Pattern.compile("\\b([0-9]{6})\\b").matcher(body);
        if (!m.find()) throw new CustomException(VerifyErrorCode.INVALID_PHONE);
        return m.group(1);
    }

    // 최소 JSON 페이로드 (필요시 필드 더 추가)
    public record InboundPayload(
            @NotBlank String body,   // 메시지 본문
            String from,             // 발신자(전화번호 or 이메일)
            String to,               // 수신자
            String subject           // 제목(이메일일 때)
    ) {}



}