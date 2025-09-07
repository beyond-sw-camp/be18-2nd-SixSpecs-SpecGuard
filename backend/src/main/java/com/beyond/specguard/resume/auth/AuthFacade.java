package com.beyond.specguard.resume.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class AuthFacade {

    private static final String HEADER = "X-Resume-Id";

    public UUID requireResumeId(HttpServletRequest req) {
        String raw = req.getHeader(HEADER);
        if (raw == null || raw.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "요청 헤더 '" + HEADER + "' 가 필요합니다.");
        }
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'" + HEADER + "' 는 UUID 형식이어야 합니다.");
        }
    }
}
