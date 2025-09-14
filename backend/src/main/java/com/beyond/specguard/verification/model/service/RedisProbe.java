package com.beyond.specguard.verification.model.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisProbe {
    private final org.springframework.data.redis.connection.RedisConnectionFactory f;
    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void ready() {
        var l = (org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory) f;
        log.info("REDIS host={} port={} db={}", l.getHostName(), l.getPort(), l.getDatabase());
    }
}
