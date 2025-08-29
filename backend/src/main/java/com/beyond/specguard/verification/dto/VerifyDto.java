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
            String token,         // 화면 안내용(본문에 들어감)
            String qrSmsto,       // QR 생성에 쓸 문자열 (SMSTO:...)
            String smsLink,       // 모바일에서 열 경우 'sms:...' 링크
            String manualTo,      // 수신처 텍스트(복사용)
            String manualBody,    // 본문 텍스트(복사용)
            long   expiresInSec
    ) {}

    public record VerifyPollResponse(
            String token, String status // PENDING | SUCCESS | FAIL | EXPIRED
    ) {}

    public record VerifyFinishRequest(
            @NotBlank(message = "token required")
            @Pattern(regexp = "^[0-9]{6}$", message = "token must be 6 digits")
            String token,

            @NotBlank(message = "phone required")
            String phone
    ) {}

    public record VerifyStatusResponse(String status) {}


    public enum VerifyChannel { EMAIL_SMSTO, NUMBER_SMSTO }
}
