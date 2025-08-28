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
            String tid,           // verification id
            String token,         // 화면 안내용(본문에 들어감)
            String qrSmsto,       // QR 생성에 쓸 문자열 (SMSTO:...)
            String smsLink,       // 모바일에서 열 경우 'sms:...' 링크
            String manualTo,      // 수신처 텍스트(복사용)
            String manualBody,    // 본문 텍스트(복사용)
            long   expiresInSec
    ) {}

    public record VerifyPollResponse(
            String tid, String status // PENDING | SUCCESS | FAIL | EXPIRED
    ) {}

    public record VerifyFinishRequest(
            @JsonAlias({"id","ID","TID"})                 // 호환 키 허용 (중요)
            @NotBlank(message = "tid required")
            @Pattern(regexp = "^[0-9a-fA-F\\-]{20,}$",    // UUID v7 대충 커버 (원하면 정확 regex로)
                    message = "tid invalid")
            String tid,

            @NotBlank(message = "token required")
            @Pattern(regexp = "^VERIFY[A-Z0-9]{6}$",      // 여러분 규칙에 맞게
                    message = "token invalid")
            String token,

            String phone
    ) {}

    public enum VerifyChannel { EMAIL_SMSTO, NUMBER_SMSTO }





}
