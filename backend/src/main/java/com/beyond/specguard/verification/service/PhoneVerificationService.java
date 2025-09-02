package com.beyond.specguard.verification.service;

import com.beyond.specguard.verification.dto.VerifyDto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import static com.beyond.specguard.verification.util.PhoneUtil.normalizePhone;

@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private final StringRedisTemplate redis;

    @Value("${verify.ttl-seconds:300}")
    private long ttlSeconds;

    @Value("${verify.max-attempts:5}")
    private int maxAttempts;

    @Value("${verify.receiver.email:specguard55@gmail.com}")
    private String emailReceiver;

    private static String attemptKey(String p)  { return "verif:attempt:" + p; }
    private static String phoneKey(String p)    { return "verif:phone:" + p; }
    private String norm(String raw) { return normalizePhone(raw); }

    private static final long SUCCESS_GRACE_SECONDS = 60;
    private Duration ttl() { return Duration.ofSeconds(ttlSeconds); }

    private static final String START_LUA = """
        -- KEYS[1] = verif:phone:{phone}
        -- KEYS[2] = verif:attempt:{phone}
        -- ARGV[1] = token
        -- ARGV[2] = ttl(sec)

        -- 상태 생성/갱신 (이전에 PENDING이 있어도 새 토큰으로 덮어쓰기)
        redis.call('HSET', KEYS[1],
        'token', ARGV[1],
        'status', 'PENDING',
        'createdAt', tostring(redis.call('TIME')[1])
        )
        redis.call('EXPIRE', KEYS[1], tonumber(ARGV[2]))
        redis.call('SET', KEYS[2], '0', 'EX', tonumber(ARGV[2]))
        return 'OK'
        """;

    private static final String FINISH_LUA = """
        -- KEYS[1] = verif:phone:{phone}
        -- KEYS[2] = verif:attempt:{phone}
        -- ARGV[1] = token
        -- ARGV[2] = maxAttempts

        local h = redis.call('HGETALL', KEYS[1])
        if #h == 0 then return 'NONE' end

        local t = {}
        for i=1,#h,2 do t[h[i]] = h[i+1] end

        if t['status'] ~= 'PENDING' then
        return t['status'] or 'FAILED'
        end

        if t['token'] ~= ARGV[1] then
        local a = tonumber(redis.call('GET', KEYS[2]) or '0') + 1
        redis.call('SET', KEYS[2], tostring(a), 'KEEPTTL')
        if a >= tonumber(ARGV[2]) then
            redis.call('HSET', KEYS[1], 'status', 'FAILED')
            return 'FAILED'
        end
        return 'FAILED'
        end

        redis.call('HSET', KEYS[1], 'status', 'SUCCESS')
        return 'SUCCESS'
        """;

    // ===== Start: 토큰 발급 =====
    @Transactional
    public VerifyDto.VerifyStartResponse start(VerifyDto.VerifyStartRequest req) {
        final String phone = norm(req.phone());
        final String token = generateToken();

        var startScript = new DefaultRedisScript<>(START_LUA, String.class);
        redis.execute(startScript, List.of(phoneKey(phone), attemptKey(phone)), token, String.valueOf(ttlSeconds));

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
        final String phone = norm(phoneRaw);
        Map<Object, Object> map = redis.opsForHash().entries(phoneKey(phone));
        if (map == null || map.isEmpty()) return new VerifyDto.VerifyStatusResponse("NONE");
        String status = (String) map.getOrDefault("status", "PENDING");
        return new VerifyDto.VerifyStatusResponse(status);
    }

    // ===== FINISH =====
    public VerifyDto.FinishResponse finish(VerifyDto.VerifyFinishRequest req) {
        final String phone = norm(req.phone());
        final String token = req.token();

        var finishScript = new DefaultRedisScript<>(FINISH_LUA, String.class);
        String result = redis.execute(finishScript, List.of(phoneKey(phone), attemptKey(phone)),
                token, String.valueOf(maxAttempts));
        // 항상 200 + {status} 로 응답하는 컨벤션 유지
        return new VerifyDto.FinishResponse(result == null ? "FAILED" : result);
    }

    // ===== POLL =====
    public VerifyDto.VerifyPollResponse poll(String phoneRaw) {
        final String phone = norm(phoneRaw);
        Map<Object, Object> payload = redis.opsForHash().entries(phoneKey(phone));
        if (payload == null || payload.isEmpty())
            return new VerifyDto.VerifyPollResponse(null, "NONE");
        String token = (String) payload.getOrDefault("token", "");
        String status = (String) payload.getOrDefault("status", "PENDING");
        return new VerifyDto.VerifyPollResponse(token, status);
    }

    // Helpers

    private String generateToken() {
        return RandomStringUtils.randomNumeric(6);
    }
}
