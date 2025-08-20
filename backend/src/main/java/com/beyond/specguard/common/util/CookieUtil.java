package com.beyond.specguard.common.util;

import jakarta.servlet.http.Cookie;

public class CookieUtil {

    public static Cookie createHttpOnlyCookie(String key, String value, int maxAge) {
        Cookie cookie = new Cookie(key, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);          // HTTPS 환경에서만 전송
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);        // 초 단위

        // ✅ SameSite=None 설정 (크로스 도메인 환경 고려)
        cookie.setAttribute("SameSite", "None");

        return cookie;
    }

    public static Cookie deleteCookie(String key) {
        Cookie cookie = new Cookie(key, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        // ✅ SameSite=None 일관성 유지
        cookie.setAttribute("SameSite", "None");

        return cookie;
    }
}
