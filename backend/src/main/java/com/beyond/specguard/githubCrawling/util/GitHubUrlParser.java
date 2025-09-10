package com.github.github.util;

public class GitHubUrlParser {

    public static String extractUsername(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("GitHub URL이 비어있습니다.");
        }

        // "https://github.com/jjjjungw" 형식 처리
        String[] parts = url.trim().split("/");
        if (parts.length < 4) {
            throw new IllegalArgumentException("잘못된 GitHub URL 형식입니다. 예: https://github.com/username");
        }

        return parts[3]; // https://github.com/{username}
    }
}
