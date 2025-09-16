package com.beyond.specguard.verification.controller;

import com.beyond.specguard.verification.model.dto.VerifyDto;
import com.beyond.specguard.verification.model.entity.EmailVerifyStatus;
import com.beyond.specguard.verification.model.repository.ApplicantEmailVerificationRepo;
import com.beyond.specguard.verification.model.repository.CompanyEmailVerificationRepo;
import com.beyond.specguard.verification.model.service.EmailVerificationService;
import com.beyond.specguard.verification.model.type.VerifyTarget;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/verify")
@RequiredArgsConstructor
@Tag(name = "Email Verification", description = "지원자/기업 이메일 인증 API")
public class EmailVerificationController {

    private final EmailVerificationService svc;

    private final CompanyEmailVerificationRepo companyRepo;
    private final ApplicantEmailVerificationRepo applicantRepo;
    private static String norm(String e){ return e==null? null : e.trim().toLowerCase(); }
    private static VerifyTarget parse(String type) {
        return "company".equalsIgnoreCase(type) ? VerifyTarget.COMPANY : VerifyTarget.APPLICANT;
    }

    @Operation(summary = "인증코드 요청")
    @PostMapping("/{type}/request")
    public ResponseEntity<Void> request(
            @PathVariable String type,
            @Valid @RequestBody VerifyDto.EmailRequest req,
            HttpServletRequest http) {
        var t = parse(type);

        String ip = Optional.ofNullable(http.getHeader("X-Forwarded-For"))
                .orElseGet(http::getRemoteAddr);
        svc.requestCode(req.email(), t, ip, req.resumeId());
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "인증코드 검증")
    @PostMapping("/{type}/confirm")
    public ResponseEntity<VerifyDto.VerifyResult> confirm(
            @PathVariable String type,
            @Valid @RequestBody VerifyDto.EmailConfirm req) {
        var t = parse(type);

        boolean ok = svc.verify(req.email(), req.code(), t, req.resumeId());
        return ResponseEntity.ok(ok ? VerifyDto.VerifyResult.ok()
                : new VerifyDto.VerifyResult("FAIL","not verified"));
    }

    @GetMapping("/{type}/status")
    public Map<String, Object> status(
            @PathVariable String type,
            @RequestParam String email,
            @Nullable UUID resumeId){
        String em = norm(email);
        var t = parse(type);

        if (t == VerifyTarget.APPLICANT && resumeId == null) {
            return Map.of("verified", false);
        }

        boolean verified = switch (t) {
            case COMPANY -> companyRepo.findByEmail(em)
                    .map(v -> v.getStatus() == EmailVerifyStatus.VERIFIED).orElse(false);
            case APPLICANT -> applicantRepo.findByEmailAndResumeId(em, resumeId)
                    .map(v -> v.getStatus() == EmailVerifyStatus.VERIFIED).orElse(false);
        };
        return Map.of("verified", verified);
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
