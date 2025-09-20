package com.beyond.specguard.validation.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;

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
        //유니코드 정규화
        String x = Normalizer.normalize(s, Normalizer.Form.NFKC).trim();
        //문장부호 공백 처리
        x = SEP.matcher(x).replaceAll(" ");
        //공백 하나로
        x = x.replaceAll("\\s+", " ");
        //소문자로
        return x.toLowerCase(Locale.ROOT);
    }

    //GITHUB_KEYWORD_MATCH
    //NOTION_KEYWORD_MATCH
    //VELOG_KEYWORD_MATCH
    //{"keywords":[...]} 또는 배열/문자열을 키워드 집합으로
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

    //GITHUB_TOPIC_MATCH
    //{"tech":[...]} 또는 배열을 기술셋으로 파싱
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

    //GITHUB_REPO_COUNT
    //{"repos":["12"]}
    public static int repoCount(String json) {
        if(json == null || json.isBlank()) return 0;
        try {
            JsonNode root = OM.readTree(json);
            JsonNode node = root.get("repos");
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


    //GITHUB_COMMIT_COUNT
    //{"commits": ["12"]}
    public static int commitCount(String json) {
        if(json == null || json.isBlank()) return 0;
        try {
            JsonNode root = OM.readTree(json);
            JsonNode node = root.get("commits");
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

    //VELOG_POST_COUNT
    // {"count":[ "12" ]}
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

    //VELOG_RECENT_ACTIVITY
    //dateCount
    public static int dateCount(String json) {
        if (json == null || json.isBlank()) return 0;
        try{
            JsonNode root = OM.readTree(json);
            JsonNode node = root.get("dateCount");
            if(node != null&& node.isArray() && node.size() > 0){
                JsonNode v = node.get(0);
                if (v.isInt()) return v.asInt();
                if (v.isTextual()) {
                    String digits = v.asText().replaceAll("[^0-9]", "");
                    if (!digits.isEmpty()) return Integer.parseInt(digits);
                }
            }return 0;
        }catch(Exception e){
            return 0;
        }
    }

    // 자카드 유사도
    //두 집합의 교집합 크기를 합집합 크기로 나눈 값
    public static double jaccard(Set<String> a, Set<String> b) {
        if (a.isEmpty() && b.isEmpty()) return 1.0;
        if (a.isEmpty() || b.isEmpty()) return 0.0;
        Set<String> inter = new HashSet<>(a); inter.retainAll(b);
        Set<String> union = new HashSet<>(a); union.addAll(b);
        return union.isEmpty() ? 0.0 : (double) inter.size() / (double) union.size();
    }


    public static Set<String> splitCsvToSet(String csv) {
        if (csv == null || csv.isBlank()) return Set.of();
        String[] parts = csv.split(",");
        Set<String> out = new LinkedHashSet<>();
        for (String p : parts) {
            String norm = normalizeToken(p);
            if (!norm.isBlank()) out.add(norm);
        }
        return out;
    }
}