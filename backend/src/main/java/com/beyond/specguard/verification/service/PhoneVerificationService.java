package com.beyond.specguard.verification.service;

import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.common.exception.errorcode.VerifyErrorCode;
import com.beyond.specguard.verification.dto.VerifyDto;
import com.beyond.specguard.verification.entity.PhoneVerification;
import com.beyond.specguard.verification.repository.PhoneVerificationRepo;
import com.beyond.specguard.verification.util.ImapReader;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import com.github.f4b6a3.uuid.UuidCreator;

@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private final PhoneVerificationRepo repo;
    private final ImapReader imap;

    @Value("${verify.ttl-seconds:300}")
    private long ttl;

    @Value("${verify.receiver.email:specguard55@gmail.com}")
    private String emailReceiver;

    @Value("${verify.receiver.sms:01034696728}")
    private String smsReceiver;


    /**
     * 충돌 방지를 위해 DB에 존재 여부를 확인하면서 토큰 생성.
     * 충돌은 드물지만 방어적으로 최대 5회 재시도.
     */
    /// //////////////////////////////////////////////
    private String generateUniqueToken() {
        for (int i = 0; i < 5; i++) {
            String t = "VERIFY" + RandomStringUtils.randomAlphanumeric(6).toUpperCase();
            if (!repo.existsByToken(t)) return t;
        }
        throw new IllegalStateException("TOKEN_GENERATION_FAILED");
    }

    @Transactional
    public VerifyDto.VerifyStartResponse start(VerifyDto.VerifyStartRequest req, String userIdOpt) {

        // 요청마다 UUIDv7 + 중복 없는 토큰 생성
        String id = UuidCreator.getTimeOrdered().toString();
        String token = generateUniqueToken();
        String normalizedPhone = normalizePhone(req.phone());

        PhoneVerification v = new PhoneVerification();
        v.setId(id);
        v.setUserId(userIdOpt);
        v.setPhone(normalizedPhone);            // ★ 정규화 저장
        v.setToken(token);
        v.setChannel(req.channel().name());
        v.setStatus("PENDING");
        v.setExpiresAt(Instant.now().plusSeconds(ttl));
        repo.save(v);

        repo.expirePendingByPhoneExceptId(normalizedPhone, v.getId());

        String to = (req.channel() == VerifyDto.VerifyChannel.EMAIL_SMSTO) ? emailReceiver : smsReceiver;
        String body = URLEncoder.encode(token, StandardCharsets.UTF_8);

        return new VerifyDto.VerifyStartResponse(
                id, token,
                "SMSTO:" + to + ":" + token,
                "sms:" + to + "?body=" + body,
                to, token, ttl
        );
    }

    @Transactional
    public VerifyDto.VerifyPollResponse poll(String tid) {
        var vOpt = repo.findById(tid);
        if (vOpt.isEmpty()) throw new IllegalArgumentException("not found");
        var v = vOpt.get();

        if ("SUCCESS".equals(v.getStatus())) {
            return new VerifyDto.VerifyPollResponse(tid, "SUCCESS");
        }

        Instant now = Instant.now();

        // 만료 처리: PENDING이면서 만료된 경우만 EXPIRED로 전이
        if (now.isAfter(v.getExpiresAt())) {
            repo.markExpiredIfPending(tid, now);
            return new VerifyDto.VerifyPollResponse(tid, "EXPIRED");
        }

        // IMAP에서 토큰 매칭 확인
        var match = imap.findToken(v.getToken());
        if (match.isPresent()) {
            int updated = repo.markIfPending(tid, "SUCCESS", now, now);
            if (updated == 1) {
                return new VerifyDto.VerifyPollResponse(tid, "SUCCESS");
            } else {
                // 경합: 최신 상태 반환
                var latest = repo.findById(tid).orElse(v);
                String status = latest.getStatus();
                if (!"SUCCESS".equals(status) && now.isAfter(latest.getExpiresAt())) {
                    status = "EXPIRED";
                }
                return new VerifyDto.VerifyPollResponse(tid, status);
            }
        }

        return new VerifyDto.VerifyPollResponse(tid, "PENDING");
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void cleanup() {
        repo.cleanup(Instant.now());
    }

    @Transactional
    public void finish(String id, String token, @Nullable String phoneFromClient) {
        var now = Instant.now();

        if (id == null || id.isBlank())
//            throw new VerifyNotFoundException("NOT_FOUND");
            throw new CustomException(VerifyErrorCode.INVALID_OTP_CODE);
        if (token == null || token.isBlank())
//            throw new VerifyInvalidTokenException("INVALID_TOKEN");
            throw new CustomException(VerifyErrorCode.INVALID_OTP_CODE);

        // 1) id + PENDING 으로만 조회
        PhoneVerification v = repo.findByIdAndStatus(id, "PENDING")
                .orElseThrow(() -> new CustomException(VerifyErrorCode.VERIFY_NOT_FOUND));

        // 2) 만료(now >= expiresAt)면 만료 전이 후 410
        if (!v.getExpiresAt().isAfter(now)) { // now >= expiresAt
            repo.markExpiredIfPending(id, now);
            throw new CustomException(VerifyErrorCode.OTP_EXPIRED);
        }

        // 3) 토큰 상수시간 비교
        String input = norm(token);
        String saved = norm(v.getToken());
        if (!constantTimeEquals(input, saved)) {
            throw new CustomException(VerifyErrorCode.INVALID_OTP_CODE);
        }

        // 4) phone 일치
        if (phoneFromClient != null && !normalizePhone(phoneFromClient).equals(v.getPhone())) {
            throw new CustomException(VerifyErrorCode.INVALID_PHONE);
        }

        // 5) 수신 확인 강제
        if (imap.findToken(v.getToken()).isEmpty()) {
            // 아직 메일/SMS 수신함에서 해당 토큰이 발견되지 않음
            throw new CustomException(VerifyErrorCode.DELIVERY_PENDING);
        }

        // 6) 경합 안전 성공 전이 (조건부 UPDATE)
        int updated = repo.markIfPending(id, "SUCCESS", now, now);
        if (updated != 1) {
            var latest = repo.findById(id).orElseThrow(() -> new CustomException(VerifyErrorCode.VERIFY_NOT_FOUND));
            if ("SUCCESS".equals(latest.getStatus())) return; // 이미 성공됨
            if (!latest.getExpiresAt().isAfter(now)) throw new CustomException(VerifyErrorCode.OTP_EXPIRED);
            throw new CustomException(VerifyErrorCode.INVALID_OTP_CODE);
        }

        // 7) 동일 번호의 다른 PENDING 정리
        repo.expirePendingByPhoneExceptId(v.getPhone(), v.getId());
    }


    private String normalize(String p) {
        if (p == null) return null;
        String s = p.replaceAll("[^0-9+]", "");
        // +82로 온 번호를 0으로 치환 (필요 시 규칙 조정)
        if (s.startsWith("+82")) s = "0" + s.substring(3);
        return s;
    }

    private static String norm(String s) { return s == null ? "" : s.trim().toUpperCase(); }
    private static String normalizePhone(String raw) { return raw.replaceAll("\\D", ""); }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int r = 0;
        for (int i = 0; i < a.length(); i++) r |= a.charAt(i) ^ b.charAt(i);
        return r == 0;
    }

}