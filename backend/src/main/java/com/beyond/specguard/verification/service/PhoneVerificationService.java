package com.beyond.specguard.verification.service;

import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.common.exception.errorcode.VerifyErrorCode;
import com.beyond.specguard.verification.dto.VerifyDto;
import com.beyond.specguard.verification.util.HashUtil;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private final StringRedisTemplate redis;

    @Value("${verify.ttl-seconds:300}")
    private long ttlSeconds;

    // ✅ 수신 번호 주입(수신처 문자/MMS용)
//    @Value("${verify.receiver.sms:01034696728}")
//    private String smsReceiver;

    private static final long SUCCESS_GRACE_SECONDS = 60;

    private String phoneKey(String phone) { return "pv:phone:" + HashUtil.sha256(phone); }
    private String tokenKey(String token) { return "pv:token:" + token; }
    private Duration ttl() { return Duration.ofSeconds(ttlSeconds); }

    // ===== Start: 토큰 발급 =====
    @Transactional
    public VerifyDto.VerifyStartResponse start(VerifyDto.VerifyStartRequest req, @Nullable String userId) {
        final String phone = normalizePhone(req.phone().replaceAll("[^0-9]", ""));
        final String pKey = phoneKey(phone);

        Boolean created = redis.opsForValue().setIfAbsent(pKey, "LOCK", ttl());
        if (Boolean.FALSE.equals(created)) {
            throw new CustomException(VerifyErrorCode.DUPLICATE_REQUEST);
        }

        final String token = generateToken();
        final String tKey  = tokenKey(token);
        final String now   = Instant.now().toString();

        // token -> hash
        redis.opsForHash().put(tKey, "phone", phone);
        redis.opsForHash().put(tKey, "createdAt", now);
        redis.opsForHash().put(tKey, "status", "PENDING");
        if (userId != null) redis.opsForHash().put(tKey, "userId", userId);
        redis.expire(tKey, ttl());

        // phone -> token
        redis.opsForValue().set(pKey, token, ttl());

        String smsBody = "[SpecGuard] 인증번호: " + token + " (5분 유효)";

        // UI/QR에 쓸 부가 문자열들 생성
        final String manualTo   = phone;
        final String manualBody = "인증번호: " + token + " (5분 유효)";
        final String qrSmsto    = "SMSTO:" + manualTo + ":" + manualBody;
        final String smsLink    = "sms:" + manualTo + "?body=" +
                java.net.URLEncoder.encode(manualBody, java.nio.charset.StandardCharsets.UTF_8);

        // DTO는 6개 필드만 (expiresAt 제거)
        return new VerifyDto.VerifyStartResponse(
                token,
                qrSmsto,
                smsLink,
                manualTo,
                manualBody,
                ttlSeconds
        );
    }

    // ===== Verify: 토큰 검증 =====
    @Transactional
    public void verify(String phone, String token) {
        final String normPhone = normalizePhone(phone);
        final String tKey = tokenKey(token);

        Map<Object, Object> payload = redis.opsForHash().entries(tKey);
        if (payload == null || payload.isEmpty()) throw new CustomException(VerifyErrorCode.OTP_EXPIRED);

        String savedPhone = (String) payload.get("phone");
        if (savedPhone == null || !savedPhone.equals(normPhone)) throw new CustomException(VerifyErrorCode.INVALID_PHONE);

        // 상태 업데이트 & 재사용 방지
        redis.opsForHash().put(tKey, "status", "VERIFIED");
        redis.delete(phoneKey(normPhone)); // 진행중 락 해제
        redis.expire(tKey, Duration.ofSeconds(SUCCESS_GRACE_SECONDS)); // 60초 동안 SUCCESS 상태 보존
    }

    // ===== Resend: 기존 건 삭제 후 재발급 =====
    @Transactional
    public VerifyDto.VerifyStartResponse resend(String phone, @Nullable String userId) {
        final String normPhone = normalizePhone(phone);
        final String pKey = phoneKey(normPhone);
        String oldToken = redis.opsForValue().get(pKey);
        if (oldToken != null) redis.delete(tokenKey(oldToken));
        redis.delete(pKey);

        VerifyDto.VerifyStartRequest req = new VerifyDto.VerifyStartRequest(normPhone, VerifyDto.VerifyChannel.NUMBER_SMSTO);
        // 또는 필요에 따라 채널 값을 EMAIL_SMSTO 로
        return start(req, userId);
    }

    // ===== Poll =====
    public VerifyDto.VerifyPollResponse poll(String token) {
        final String tKey = tokenKey(token);
        Map<Object, Object> payload = redis.opsForHash().entries(tKey);
        if (payload == null || payload.isEmpty()) return new VerifyDto.VerifyPollResponse(token, "EXPIRED");
        String status = (String) payload.getOrDefault("status", "PENDING");
        return new VerifyDto.VerifyPollResponse(token, status);
    }

    // 컨트롤러의 finish(...) 호출을 살리기 위한 어댑터 (tid는 무시)
    @Transactional
    public void finish(String tid, String token, @Nullable String phone) {
        // Redis 전환 이후엔 tid가 의미 없으므로 token+phone 검증만 수행
        verify(phone, token);
    }

    // Helpers
    private String generateToken() { return RandomStringUtils.randomNumeric(6); }
    private static String normalizePhone(String raw) {
        if (raw == null) return null;
        String s = raw.replaceAll("[^0-9+]", "");
        if (s.startsWith("+82")) s = "0" + s.substring(3);
        return s.replaceAll("\\D", "");
    }
}
