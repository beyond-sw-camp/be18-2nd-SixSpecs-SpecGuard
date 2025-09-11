package com.beyond.specguard.verification.model.repository;

import com.beyond.specguard.common.properties.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class VerificationRedisRepository {
    private final StringRedisTemplate redis;
    private final AppProperties appProperties;

    private String attemptKey(String p)  {
        return appProperties.getRedis().getPrefix().getVerifyAttempt() + p;
    }

    private String phoneKey(String p)    {
        return appProperties.getRedis().getPrefix().getVerifyPhone() + p;
    }

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

    public void start(String phone, String token, long ttlSeconds) {
        DefaultRedisScript<String> startScript = new DefaultRedisScript<>(START_LUA, String.class);
        redis.execute(startScript, List.of(phoneKey(phone), attemptKey(phone)), token, String.valueOf(ttlSeconds));

    }

    public Map<Object, Object> get(String phone) {
        return redis.opsForHash().entries(phoneKey(phone));
    }

    public String finish(String phone, String token, int maxAttempts) {
        var finishScript = new DefaultRedisScript<>(FINISH_LUA, String.class);
        return redis.execute(finishScript, List.of(phoneKey(phone), attemptKey(phone)),
                token, String.valueOf(maxAttempts));
    }
}
