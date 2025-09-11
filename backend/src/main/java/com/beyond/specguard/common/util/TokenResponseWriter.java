<<<<<<< HEAD
package com.beyond.specguard.common.util;

import com.beyond.specguard.auth.model.dto.response.ReissueResponseDto;
import com.beyond.specguard.common.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenResponseWriter {
    private final JwtUtil jwtUtil;
    public void writeTokens(HttpServletResponse response, ReissueResponseDto dto){
        response.setHeader("Authorization", "Bearer " + dto.getAccessToken());

        int maxAge = (int)((jwtUtil.getExpiration(dto.getRefreshToken()).getTime() - System.currentTimeMillis()) / 1000L);

        Cookie cookie = CookieUtil.createHttpOnlyCookie(
                "refresh_token",
                dto.getRefreshToken(),
                maxAge
                );
        response.addCookie(cookie);
    }
}
=======
package com.beyond.specguard.common.util;

import com.beyond.specguard.auth.model.dto.response.ReissueResponseDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenResponseWriter {
    private final JwtUtil jwtUtil;
    public void writeTokens(HttpServletResponse response, ReissueResponseDto dto){
        response.setHeader("Authorization", "Bearer " + dto.getAccessToken());

        int maxAge = (int)((jwtUtil.getExpiration(dto.getRefreshToken()).getTime() - System.currentTimeMillis()) / 1000L);

        Cookie cookie = CookieUtil.createHttpOnlyCookie(
                "refresh_token",
                dto.getRefreshToken(),
                maxAge
                );
        response.addCookie(cookie);
    }
}
>>>>>>> develop
