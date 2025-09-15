package com.beyond.specguard.verification.controller;

import com.beyond.specguard.verification.model.dto.VerifyDto;
import com.beyond.specguard.verification.model.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/resumes/verify/email")
@RequiredArgsConstructor
@Tag(name = "Email Verification", description = "지원자 회원가입 시 이메일 인증 API")
public class EmailVerificationController {

    private final EmailVerificationService svc;

    @Operation(
            summary = "인증코드 요청",
            description = "지원자가 이메일을 입력하면 인증코드(OTP)를 발송합니다.",
            responses = {
                    @ApiResponse(responseCode = "202", description = "인증코드 발송 요청 수락",
                            content = @Content(schema = @Schema(implementation = VerifyDto.EmailRequest.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청")
            }
    )
    @PostMapping("/request")
    public ResponseEntity<Void> request(@RequestBody VerifyDto.EmailRequest req) throws IOException {
        log.info("verify.request email={}", req.email()); // POJO면 req.getEmail()
        if (req.email() == null || req.email().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        svc.requestCode(req.email());
        return ResponseEntity.accepted().build();
    }

    @Operation(
            summary = "인증코드 검증",
            description = "이메일과 인증코드를 검증합니다."
    )
    @PostMapping("/confirm")
    public ResponseEntity<VerifyDto.VerifyResult> confirm(@RequestBody VerifyDto.EmailConfirm req) {
        boolean ok = svc.verify(req.email(), req.code()); // POJO일 경우 getEmail(), getCode()
        if (ok) return ResponseEntity.ok(VerifyDto.VerifyResult.ok());
        return ResponseEntity.ok(new VerifyDto.VerifyResult("FAIL", "not verified"));
    }



    //임시 확인용
    @GetMapping("/api/v1/verify/email/_redis")
    public Map<String,Object> redisInfo(
            org.springframework.data.redis.core.StringRedisTemplate redis) {
        var f = (org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory)
                redis.getConnectionFactory();
        return Map.of("host", f.getHostName(), "port", f.getPort(), "db", f.getDatabase());
    }

    @GetMapping("/api/v1/verify/email/_peek")
    public Map<String,Object> peek(
            @RequestParam String email,
            org.springframework.data.redis.core.StringRedisTemplate redis) {
        var k = "verif:email:" + email.toLowerCase();
        return Map.of("key", k, "val", redis.opsForValue().get(k), "ttl", redis.getExpire(k));
    }
}

