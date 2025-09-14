package com.beyond.specguard.verification.util;

public class EmailUtil {
    public static String normalizeEmail(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase();
        int sp = s.indexOf('+');
        int at = s.indexOf('@');
        if (at > 0 && sp > -1 && sp < at && s.endsWith("@gmail.com")) {
            s = s.substring(0, sp) + s.substring(at); // Gmail 플러스태그 제거
        }
        return s;
    }
}
