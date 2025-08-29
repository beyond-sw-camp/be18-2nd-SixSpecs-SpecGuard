package com.beyond.specguard.verification.util;

public final class PhoneUtil {
    private PhoneUtil() {}
    public static String normalizePhone(String raw) {
        if (raw == null) return null;
        String s = raw.replaceAll("[^0-9+]", "");
        if (s.startsWith("+82")) s = "0" + s.substring(3);
        s = s.replaceAll("\\D", "");
        // 휴대폰으로 볼 수 없는 길이면 빈값
        if (s.length() < 10 || s.length() > 11) return "";
        return s;
//        return s.replaceAll("\\D", "");
    }
}
