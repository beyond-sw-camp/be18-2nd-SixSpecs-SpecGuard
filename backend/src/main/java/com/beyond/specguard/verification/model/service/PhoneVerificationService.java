package com.beyond.specguard.verification.model.service;

import com.beyond.specguard.common.properties.AppProperties;
import com.beyond.specguard.verification.model.dto.VerifyDto;
import com.beyond.specguard.verification.model.repository.VerificationRedisRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.beyond.specguard.verification.util.PhoneUtil.normalizePhone;

@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private final AppProperties appProperties;

    private final VerificationRedisRepository verificationRedisRepository;

    // ===== Start: 토큰 발급 =====
    @Transactional
    public VerifyDto.VerifyStartResponse start(VerifyDto.VerifyStartRequest req) {
        final String phone = normalizePhone(req.phone());
        final String token = generateToken();
        long ttlSeconds = appProperties.getVerify().getTtlSeconds();
        String emailReceiver = appProperties.getVerify().getReceiverEmail();

        verificationRedisRepository.start(phone, token, ttlSeconds);

        // 사용자에게 보낼 본문(이메일/SMS)
        String manualBody = "[SpecGuard] 본인인증번호: " + token + " (" + (ttlSeconds / 60) +
                "분 유효)\n" + "이 메시지를 그대로 보내 인증을 완료해 주세요.";

        String smsLink = "sms:" + emailReceiver + "?body=" +
                URLEncoder.encode(manualBody, StandardCharsets.UTF_8);
        String qrSmsto = "SMSTO:" + emailReceiver + ":" + manualBody;

        // 프런트가 쓰는 필드들 반환
        return new VerifyDto.VerifyStartResponse(
                token,
                smsLink,
                qrSmsto,
                emailReceiver,
                manualBody,
                ttlSeconds
        );
    }

    // ===== STATUS =====
    public VerifyDto.VerifyStatusResponse status(String phoneRaw) {
        final String phone = normalizePhone(phoneRaw);

        Map<Object, Object> map = verificationRedisRepository.get(phone);

        if (map == null || map.isEmpty()) {
            return new VerifyDto.VerifyStatusResponse("NONE");
        }

        String status = (String) map.getOrDefault("status", "PENDING");

        return new VerifyDto.VerifyStatusResponse(status);
    }

    // ===== FINISH =====
    public VerifyDto.FinishResponse finish(VerifyDto.VerifyFinishRequest req) {
        final String phone = normalizePhone(req.phone());
        final String token = req.token();
        final int maxAttempts = appProperties.getVerify().getMaxAttempts();

        String result = verificationRedisRepository.finish(phone, token, maxAttempts);

        // 항상 200 + {status} 로 응답하는 컨벤션 유지
        return new VerifyDto.FinishResponse(result == null ? "FAILED" : result);
    }

    // ===== POLL =====
    public VerifyDto.VerifyPollResponse poll(String phoneRaw) {
        final String phone = normalizePhone(phoneRaw);

        Map<Object, Object> payload = verificationRedisRepository.get(phone);

        if (payload == null || payload.isEmpty()) {
            return new VerifyDto.VerifyPollResponse(null, "NONE");
        }

        String token = (String) payload.getOrDefault("token", "");
        String status = (String) payload.getOrDefault("status", "PENDING");

        return new VerifyDto.VerifyPollResponse(token, status);
    }

    // Helpers
    private String generateToken() {
        return RandomStringUtils.randomNumeric(6);
    }
}
