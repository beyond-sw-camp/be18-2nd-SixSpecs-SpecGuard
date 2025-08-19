package com.beyond.specguard.common.util;

import jakarta.servlet.http.Cookie;

public class CookieUtil {

    public static Cookie createHttpOnlyCookie(String key, String value, int maxAge) {
        Cookie cookie = new Cookie(key, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);       // HTTPS 환경에서만 전송
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);     // 초 단위
        return cookie;
    }

    public static Cookie deleteCookie(String key) {
        Cookie cookie = new Cookie(key, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }
}
