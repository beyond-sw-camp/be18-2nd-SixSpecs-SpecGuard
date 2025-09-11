package com.beyond.specguard.verification.controller;

import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.verification.exception.errorcode.VerifyErrorCode;
import com.beyond.specguard.verification.model.dto.VerifyDto;
import com.beyond.specguard.verification.model.service.PhoneVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.beyond.specguard.verification.util.PhoneUtil.normalizePhone;

@RestController
@RequestMapping("/api/v1/verify/phone")
@RequiredArgsConstructor
@Tag(name = "Phone Verification", description = "지원자 회원가입 시 휴대폰 인증 API")
public class PhoneVerificationController {

    private final PhoneVerificationService service;

    @Operation(
            summary = "인증코드 요청",
            description = "지원자가 휴대폰 번호를 입력하면 인증코드(OTP)를 발송합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "인증코드 발송 성공",
                            content = @Content(schema = @Schema(implementation = VerifyDto.VerifyStartResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청")
            }
    )
    @PostMapping("/start")
    public VerifyDto.VerifyStartResponse start(
            @Valid @RequestBody VerifyDto.VerifyStartRequest req
    ) {
        return service.start(req);
    }

    @Operation(
            summary = "인증 진행 상태 폴링",
            description = "UI에서 2~3초 간격으로 호출하여 인증 완료 여부를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "인증 상태 반환",
                            content = @Content(schema = @Schema(implementation = VerifyDto.VerifyPollResponse.class)))
            }
    )
    @GetMapping("/poll")
    public VerifyDto.VerifyPollResponse poll(
            @RequestParam("phone") String phone
    ) {
        return service.poll(phone);
    }

    // 3) 최종 인증 — phone + token
    @Operation(
            summary = "최종 인증 완료",
            description = "지원자가 받은 인증코드(OTP)를 입력하여 최종 인증을 완료합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "인증 성공",
                            content = @Content(schema = @Schema(implementation = VerifyDto.FinishResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 인증코드")
            }
    )
    @PostMapping("/finish")
    public VerifyDto.FinishResponse finish(
            @Valid @RequestBody VerifyDto.VerifyFinishRequest req
    ) {
        return service.finish(req);
    }

    // 4) 상태 조회 — phone 기준
    @Operation(
            summary = "인증 상태 조회",
            description = "휴대폰 번호 기준으로 현재 인증 상태를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "인증 상태 반환",
                            content = @Content(schema = @Schema(implementation = VerifyDto.VerifyStatusResponse.class)))
            }
    )
    @GetMapping("/status")
    public VerifyDto.VerifyStatusResponse status(
            @RequestParam("phone") String phone
    ) {
        return service.status(phone);
    }

    /*
      5) SMS/메일 웹훅 수신
        - 경로 충돌 방지를 위해 /inbound 로 분리
        - start 시 manualBody에 "PHONE:{phone}"를 포함하면 본문에서 추출 가능
     */
    @Operation(
            summary = "SMS/메일 웹훅 수신",
            description = """
                    외부 SMS/Mail Provider 에서 전송한 Webhook을 수신합니다.
                    - `manualBody`에 "PHONE:{phone}"를 포함하면 본문에서 추출 가능합니다.
                    - 인증코드를 본문에서 추출하여 자동으로 finish 처리합니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "202", description = "Webhook 처리 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청")
            }
    )
    @PostMapping(value = "/inbound", consumes = "application/json")
    public ResponseEntity<Void> inbound(
            @RequestBody @Valid InboundPayload p,
            @RequestHeader(value = "X-Verify-Secret", required = false) String secret
    ) {
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
