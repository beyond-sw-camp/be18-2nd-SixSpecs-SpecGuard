package com.beyond.specguard.verification.controller;

import com.beyond.specguard.verification.model.dto.VerifyDto;
import com.beyond.specguard.verification.model.service.EmailVerificationService;
import com.beyond.specguard.verification.model.type.VerifyTarget;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/verify/email")
@RequiredArgsConstructor
@Validated
@Tag(name = "Email Verification", description = "지원자/기업 이메일 인증 API")
public class EmailVerificationController {

    private final EmailVerificationService svc;

    private static VerifyTarget parse(String type) {
        return "company".equalsIgnoreCase(type) ? VerifyTarget.COMPANY : VerifyTarget.APPLICANT;
    }

    @Operation(summary = "인증코드 요청")
    @PostMapping("/{type}/request") // type = applicant | company
    public ResponseEntity<Void> request(
            @PathVariable String type,
            @Valid @RequestBody VerifyDto.EmailRequest req,
            HttpServletRequest http
    ) {
        log.info("verify.request type={} email={}", type, req.email());
        String ip = Optional.ofNullable(http.getHeader("X-Forwarded-For"))
                .orElseGet(http::getRemoteAddr);
        svc.requestCode(req.email(), parse(type), ip);
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "인증코드 검증")
    @PostMapping("/{type}/confirm") // type = applicant | company
    public ResponseEntity<VerifyDto.VerifyResult> confirm(
            @PathVariable String type,
            @Valid @RequestBody VerifyDto.EmailConfirm req
    ) {
        boolean ok = svc.verify(req.email(), req.code(), parse(type));
        return ResponseEntity.ok(ok ? VerifyDto.VerifyResult.ok()
                : new VerifyDto.VerifyResult("FAIL", "not verified"));
    }

    // ===== 디버그 =====
    @GetMapping("/_redis")
    public Map<String,Object> redisInfo(StringRedisTemplate redis) {
        var f = (LettuceConnectionFactory) redis.getConnectionFactory();
        return Map.of("host", f.getHostName(), "port", f.getPort(), "db", f.getDatabase());
    }

    @GetMapping("/_peek")
    public Map<String,Object> peek(@RequestParam String email, StringRedisTemplate redis) {
        var k = "verif:email:" + email.toLowerCase();
        return Map.of("key", k, "val", redis.opsForValue().get(k), "ttl", redis.getExpire(k));
    }
}
