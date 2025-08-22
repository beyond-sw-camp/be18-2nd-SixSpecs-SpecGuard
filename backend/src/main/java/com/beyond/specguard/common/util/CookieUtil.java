package com.beyond.specguard.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class CookieUtil {

    // ✅ HttpOnly 쿠키 생성
    public static Cookie createHttpOnlyCookie(String key, String value, int maxAge) {
        Cookie cookie = new Cookie(key, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);          // HTTPS 환경에서만 전송 (로컬 개발에서는 false로 해도 됨)
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);        // 초 단위
        cookie.setAttribute("SameSite", "None"); // 크로스 도메인 환경 고려
        return cookie;
    }

    // ✅ 쿠키 삭제
    public static Cookie deleteCookie(String key) {
        Cookie cookie = new Cookie(key, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "None");
        return cookie;
    }

    // ✅ 요청에서 특정 쿠키 값 꺼내기
    public static String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
