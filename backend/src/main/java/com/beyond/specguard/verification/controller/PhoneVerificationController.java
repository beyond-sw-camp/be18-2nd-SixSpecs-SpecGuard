package com.beyond.specguard.verification.controller;

import static com.beyond.specguard.verification.util.PhoneUtil.normalizePhone;
import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.common.exception.errorcode.VerifyErrorCode;
import com.beyond.specguard.verification.dto.VerifyDto;
import com.beyond.specguard.verification.service.PhoneVerificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/verify/phone")
@RequiredArgsConstructor
public class PhoneVerificationController {


    private final PhoneVerificationService service;

    // 1) 인증코드 생성
    @PostMapping("/start")
    public VerifyDto.VerifyStartResponse start(@Valid @RequestBody VerifyDto.VerifyStartRequest req) {
        return service.start(req);
    }

    // 2) 폴링 (UI에서 2~3초 간격) — phone 기준
    @GetMapping("/poll")
    public VerifyDto.VerifyPollResponse poll(@RequestParam("phone") String phone) {
        return service.poll(phone);
    }

    // 3) 최종 인증 — phone + token
    @PostMapping("/finish")
    public VerifyDto.FinishResponse finish(@Valid @RequestBody VerifyDto.VerifyFinishRequest req) {
        return service.finish(req);
    }

    // 4) 상태 조회 — phone 기준
    @GetMapping("/status")
    public VerifyDto.VerifyStatusResponse status(@RequestParam("phone") String phone) {
        return service.status(phone);
    }

    /*
      5) SMS/메일 웹훅 수신
        - 경로 충돌 방지를 위해 /inbound 로 분리
        - start 시 manualBody에 "PHONE:{phone}"를 포함하면 본문에서 추출 가능
     */
    @PostMapping(value = "/inbound", consumes = "application/json")
    public ResponseEntity<Void> inbound(
            @RequestBody @Valid InboundPayload p,
            @RequestHeader(value = "X-Verify-Secret", required = false) String secret
    ) {
        // (선택) 웹훅 보안
        // if (!"your-secret".equals(secret)) return ResponseEntity.status(403).build();

        final String body  = p.body();
        final String token = extractSixDigits(body);

        final String from = p.from();
        if (from == null) return ResponseEntity.badRequest().build();

        final String phone = normalizePhone(from);
        if (phone.isEmpty()) return ResponseEntity.badRequest().build();

        service.finish(new VerifyDto.VerifyFinishRequest(token, phone));
        return ResponseEntity.accepted().build();
    }

    private String extractSixDigits(String text) {
        if (text == null) throw new CustomException(VerifyErrorCode.INVALID_OTP_CODE);
        Matcher m = Pattern.compile("\\b([0-9]{6})\\b").matcher(text);
        if (!m.find()) throw new CustomException(VerifyErrorCode.INVALID_OTP_CODE);
        return m.group(1);
    }

    // 웹훅 최소 페이로드
    public record InboundPayload(
            @NotBlank String body,
            String from,
            String to,
            String subject
    ) {}
}
