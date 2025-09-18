package com.beyond.specguard.validation.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class KeywordUtils {

    private static final ObjectMapper OM = new ObjectMapper();
    private static final Pattern SEP = Pattern.compile("[\\p{Punct}\\p{S}]+", Pattern.UNICODE_CHARACTER_CLASS);

    private KeywordUtils() {}

    private static String normalizeToken(String s) {
        if (s == null) return "";
        String x = Normalizer.normalize(s, Normalizer.Form.NFKC).trim();
        x = SEP.matcher(x).replaceAll(" ");
        x = x.replaceAll("\\s+", " ");
        return x.toLowerCase(Locale.ROOT);
    }

    /** {"keywords":[...]} 또는 배열/문자열을 키워드 집합으로 */
    public static Set<String> parseKeywords(String jsonOrArray) {
        if (jsonOrArray == null || jsonOrArray.isBlank()) return Set.of();
        try {
            JsonNode root = OM.readTree(jsonOrArray);
            JsonNode arr = root;
            if (root.isObject() && root.has("keywords")) arr = root.get("keywords");
            if (arr.isArray()) {
                Set<String> out = new LinkedHashSet<>();
                for (JsonNode n : arr) {
                    if (!n.isTextual()) continue;
                    String norm = normalizeToken(n.asText());
                    if (!norm.isBlank()) out.add(norm);
                }
                return out;
            }
            // fallback: 쉼표/개행 구분
            String[] parts = jsonOrArray.split("[,\\n\\t]");
            Set<String> out = new LinkedHashSet<>();
            for (String p : parts) {
                String norm = normalizeToken(p);
                if (!norm.isBlank()) out.add(norm);
            }
            return out;
        } catch (Exception e) {
            return Set.of();
        }
    }

    /** {"tech":[...]} 또는 배열을 기술셋으로 파싱(GitHub 토픽 매칭) */
    public static Set<String> parseTech(String json) {
        if (json == null || json.isBlank()) return Set.of();
        try {
            JsonNode root = OM.readTree(json);
            JsonNode arr = root;
            if (root.isObject() && root.has("tech")) arr = root.get("tech");
            if (arr != null && arr.isArray()) {
                Set<String> out = new LinkedHashSet<>();
                for (JsonNode n : arr) {
                    if (!n.isTextual()) continue;
                    String norm = normalizeToken(n.asText());
                    if (!norm.isBlank()) out.add(norm);
                }
                return out;
            }
            return Set.of();
        } catch (Exception e) {
            return Set.of();
        }
    }

    /** {"count":[ "12" ]} 같은 포맷을 정수로 (없으면 0) */
    public static int parseCount(String json) {
        if (json == null || json.isBlank()) return 0;
        try {
            JsonNode root = OM.readTree(json);
            JsonNode node = root.get("count");
            if (node != null && node.isArray() && node.size() > 0) {
                JsonNode v = node.get(0);
                if (v.isInt()) return v.asInt();
                if (v.isTextual()) {
                    String digits = v.asText().replaceAll("[^0-9]", "");
                    if (!digits.isEmpty()) return Integer.parseInt(digits);
                }
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /** 자카드 유사도 */
    public static double jaccard(Set<String> a, Set<String> b) {
        if (a.isEmpty() && b.isEmpty()) return 1.0;
        if (a.isEmpty() || b.isEmpty()) return 0.0;
        Set<String> inter = new HashSet<>(a); inter.retainAll(b);
        Set<String> union = new HashSet<>(a); union.addAll(b);
        return union.isEmpty() ? 0.0 : (double) inter.size() / (double) union.size();
    }
}
