package com.beyond.specguard.verification.controller;

import com.beyond.specguard.verification.model.dto.VerifyDto;
import com.beyond.specguard.verification.model.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/verify/email")
@RequiredArgsConstructor
@Tag(name = "Email Verification", description = "지원자 회원가입 시 이메일 인증 API")
public class EmailVerificationController {
    private final EmailVerificationService svc;

    @Operation(
            summary = "인증코드 요청",
            description = "지원자가 이메일을 입력하면 인증코드(OTP)를 발송합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "인증코드 발송 성공",
                            content = @Content(schema = @Schema(implementation = VerifyDto.EmailRequest.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청")
            }
    )


    @PostMapping("/request")
    public ResponseEntity<Void> request(@RequestBody VerifyDto.EmailRequest req) {
        svc.requestCode(req.email());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<VerifyDto.VerifyResult> confirm(@RequestBody VerifyDto.EmailConfirm req) {
        String status = svc.confirm(req.email(), req.code());
        if ("SUCCESS".equals(status)) return ResponseEntity.ok(VerifyDto.VerifyResult.ok());
        return ResponseEntity.ok(new VerifyDto.VerifyResult(status, "not verified"));
    }
}
