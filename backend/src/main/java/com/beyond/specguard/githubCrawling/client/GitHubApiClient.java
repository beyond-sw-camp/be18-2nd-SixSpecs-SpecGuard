package com.github.github.client;

import com.github.github.dto.GitHubStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class GitHubApiClient {

    private final RestTemplate githubRestTemplate;

    public GitHubStatsDto fetchGitHubStats(String username) {
        List<Map<String, Object>> repos = fetchRepositories(username);

        int totalCommits = 0;
        Map<String, Integer> languageTotals = new HashMap<>();
        Map<String, String> updatedMap = new HashMap<>();
        Map<String, String> readmeMap = new HashMap<>();

        for (Map<String, Object> repo : repos) {
            String repoName = (String) repo.get("name");

            // 기존 통계
            totalCommits += fetchUserCommitCount(username, repoName);
            mergeLanguageStats(username, repoName, languageTotals);

            // 추가: 레포 업데이트 날짜
            updatedMap.put(repoName, fetchRepoUpdatedAt(username, repoName));

            // 추가: README 내용
            readmeMap.put(repoName, fetchRepoReadme(username, repoName));
        }

        return new GitHubStatsDto(
                repos.size(),
                totalCommits,
                languageTotals,
                updatedMap,
                readmeMap
        );
    }

    // ------------------ 기존 메서드 ------------------

    private List<Map<String, Object>> fetchRepositories(String username) {
        String url = "https://api.github.com/users/" + username + "/repos";
        ResponseEntity<List<Map<String, Object>>> response = githubRestTemplate.exchange(
                url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {}
        );
        return Optional.ofNullable(response.getBody()).orElse(Collections.emptyList());
    }

    private int fetchUserCommitCount(String username, String repoName) {
        String url = "https://api.github.com/repos/" + username + "/" + repoName + "/contributors";
        ResponseEntity<List<Map<String, Object>>> response = githubRestTemplate.exchange(
                url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {}
        );

        List<Map<String, Object>> contributors = response.getBody();
        if (contributors == null) return 0;

        for (Map<String, Object> contributor : contributors) {
            if (username.equalsIgnoreCase((String) contributor.get("login"))) {
                Object contrib = contributor.get("contributions");
                return contrib instanceof Number ? ((Number) contrib).intValue() : 0;
            }
        }
        return 0;
    }

    private void mergeLanguageStats(String username, String repoName, Map<String, Integer> totalStats) {
        String url = "https://api.github.com/repos/" + username + "/" + repoName + "/languages";
        Map<String, Integer> langStats = githubRestTemplate.getForObject(url, Map.class);
        if (langStats != null) {
            for (Map.Entry<String, Integer> entry : langStats.entrySet()) {
                totalStats.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }
    }

    // ------------------ 새로 추가한 메서드 ------------------

    private String fetchRepoUpdatedAt(String username, String repoName) {
        String url = "https://api.github.com/repos/" + username + "/" + repoName;
        Map<String, Object> repoInfo = githubRestTemplate.getForObject(url, Map.class);
        if (repoInfo != null && repoInfo.get("updated_at") != null) {
            return (String) repoInfo.get("updated_at");
        }
        return null;
    }

    private String fetchRepoReadme(String username, String repoName) {
        String readmeUrl = "https://api.github.com/repos/" + username + "/" + repoName + "/readme";

        try {
            // 1. 기본 README API 호출
            Map<String, Object> readmeJson = githubRestTemplate.getForObject(readmeUrl, Map.class);

            if (readmeJson != null
                    && readmeJson.get("content") != null
                    && "base64".equals(readmeJson.get("encoding"))) {
                String base64Content = (String) readmeJson.get("content");
                return new String(Base64.getDecoder().decode(base64Content));
            }
        } catch (Exception e) {
            // 404 Not Found → contents API로 fallback
        }

        try {
            // 2. 루트 디렉토리 전체 탐색
            String contentsUrl = "https://api.github.com/repos/" + username + "/" + repoName + "/contents";
            ResponseEntity<List<Map<String, Object>>> response = githubRestTemplate.exchange(
                    contentsUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            List<Map<String, Object>> files = response.getBody();
            if (files != null) {
                for (Map<String, Object> file : files) {
                    String name = (String) file.get("name");
                    if (name != null && name.toLowerCase().startsWith("readme")) {
                        // download_url을 이용해 바로 원문 다운로드
                        String downloadUrl = (String) file.get("download_url");
                        if (downloadUrl != null) {
                            return githubRestTemplate.getForObject(downloadUrl, String.class);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // contents API 호출 실패 → 무시
        }

        // 3. README를 끝내 못 찾으면 기본 메시지 반환
        return "README 없음";
    }


}
