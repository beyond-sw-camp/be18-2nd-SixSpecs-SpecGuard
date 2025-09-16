package com.beyond.specguard.verification.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public final class VerifyDto {

    public record EmailRequest(
            @Schema(example = "specguard@specguard.com")
            @NotBlank
            @Pattern(regexp = "(?i)^[a-z0-9._%+\\-]+@[a-z0-9.-]+\\.[a-z]{2,63}$",
                    message = "유효한 이메일 형식이 아닙니다.")
            String email,

            @Schema(format = "uuid", nullable = true, description = "회원가입 단계에서는 null")
            UUID resumeId) {}

    public record EmailConfirm(
            @Schema(example = "specguard@specguard.com")
            @NotBlank(message = "email address required")
            String email,

            @Schema(example = "123456")
            @NotBlank(message = "token required")
            @Pattern(regexp = "^[0-9]{6}$", message = "token must be 6 digits")
            String code,

            @Schema(format = "uuid", nullable = true)
            UUID resumeId) {}

    // status: SUCCESS/FAIL/BLOCKED/EXPIRED/TOO_MANY_ATTEMPTS
    public record VerifyResult(String status, String message) {
        public static VerifyResult ok() { return new VerifyResult("SUCCESS", "verified"); }
        public static VerifyResult fail(String m) { return new VerifyResult("FAIL", m); }
    }
}
