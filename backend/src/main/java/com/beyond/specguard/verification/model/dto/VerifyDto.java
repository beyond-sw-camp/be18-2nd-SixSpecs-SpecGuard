package com.beyond.specguard.verification.model.dto;

public class VerifyDto {

    public record EmailRequest(String email) {}

    public record EmailConfirm(String email, String code) {}

    // status: SUCCESS/FAIL/BLOCKED/EXPIRED/TOO_MANY_ATTEMPTS
    public record VerifyResult(String status, String message) {
        public static VerifyResult ok() { return new VerifyResult("SUCCESS", "verified"); }
    }
}
