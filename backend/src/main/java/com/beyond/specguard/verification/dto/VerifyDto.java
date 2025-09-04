package com.beyond.specguard.verification.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class VerifyDto {
    public record VerifyStartRequest(
            @NotBlank
            @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$",
                    message="휴대폰 번호 형식이 아닙니다")
            String phone,             // UI에서 입력
            @NotNull VerifyChannel channel      // EMAIL SMSTO or NUMBER SMSTO
    ) {}

    public record VerifyStartResponse(
            String token,        // 표시용 (보안상 서버에서만 쓰고 프론트 미표시도 가능)
            String smsLink,      // sms:...&body=...
            String qrSmsto,      // SMSTO:...:...
            String manualTo,     // 수신자(예: 이메일)
            String manualBody,   // 본문 (VID, PHONE 포함)
            long   expiresInSec  // TTL 초
    ) {}

    public record VerifyPollResponse(
            String status, // PENDING | SUCCESS | FAIL | EXPIRED
            String token
    ) {}

    public record VerifyFinishRequest(
            @NotBlank(message = "token required")
            @Pattern(regexp = "^[0-9]{6}$", message = "token must be 6 digits")
            String token,

            @NotBlank(message = "phone required")
            String phone
    ) {}

    public record FinishResponse(String status) {}

    public record VerifyStatusResponse(String status) {}

    public enum VerifyChannel { EMAIL_SMSTO, NUMBER_SMSTO }
}
