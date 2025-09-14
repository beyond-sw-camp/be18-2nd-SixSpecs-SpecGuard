package com.beyond.specguard.verification.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class VerifyDto {

    public record EmailRequest(
            @NotBlank
            @Pattern(regexp = "(?i)^[a-z0-9._%+\\-]+@[a-z0-9.-]+\\.[a-z]{2,63}$",
                    message = "유효한 이메일 형식이 아닙니다.")
            String email) {}

    public record EmailConfirm(
            @NotBlank(message = "email address required")
            String email,

            @NotBlank(message = "token required")
            @Pattern(regexp = "^[0-9]{6}$", message = "token must be 6 digits")
            String code) {}

    // status: SUCCESS/FAIL/BLOCKED/EXPIRED/TOO_MANY_ATTEMPTS
    public record VerifyResult(String status, String message) {
        public static VerifyResult ok() { return new VerifyResult("SUCCESS", "verified"); }
        public static VerifyResult fail(String m) { return new VerifyResult("FAIL", m); }
    }
}
