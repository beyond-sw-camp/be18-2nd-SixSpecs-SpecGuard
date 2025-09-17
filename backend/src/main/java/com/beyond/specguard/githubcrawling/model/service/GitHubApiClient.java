package com.beyond.specguard.githubcrawling.model.service;

import com.beyond.specguard.common.properties.AppProperties;
import com.beyond.specguard.githubcrawling.model.dto.GitHubStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@RequiredArgsConstructor
public class GitHubApiClient {

    private final RestTemplate githubRestTemplate;
    private final AppProperties appProperties;

    // 공통 인증 헤더 생성
    private HttpEntity<Void> buildAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + appProperties.getGithub().getToken());
        return new HttpEntity<>(headers);
    }

    public GitHubStatsDto fetchGitHubStats(String username) {
        List<Map<String, Object>> repos = fetchRepositories(username);

        int totalCommits = 0;
        Map<String, Integer> languageTotals = new HashMap<>();
        Map<String, String> updatedMap = new HashMap<>();
        Map<String, String> readmeMap = new HashMap<>();

        for (Map<String, Object> repo : repos) {
            String repoName = (String) repo.get("name");

            totalCommits += fetchUserCommitCount(username, repoName);
            mergeLanguageStats(username, repoName, languageTotals);
            updatedMap.put(repoName, fetchRepoUpdatedAt(username, repoName));
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

    private List<Map<String, Object>> fetchRepositories(String username) {
        String url = "https://api.github.com/users/" + username + "/repos";
        ResponseEntity<List<Map<String, Object>>> response =
                githubRestTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        buildAuthEntity(),
                        new ParameterizedTypeReference<>() {}
                );
        return Optional.ofNullable(response.getBody()).orElse(Collections.emptyList());
    }

    private int fetchUserCommitCount(String username, String repoName) {
        String url = "https://api.github.com/repos/" + username + "/" + repoName + "/contributors";
        ResponseEntity<List<Map<String, Object>>> response =
                githubRestTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        buildAuthEntity(),
                        new ParameterizedTypeReference<>() {}
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
        ResponseEntity<Map<String, Integer>> response =
                githubRestTemplate.exchange(url, HttpMethod.GET, buildAuthEntity(), new ParameterizedTypeReference<>() {});
        Map<String, Integer> langStats = response.getBody();

        if (langStats != null) {
            for (Map.Entry<String, Integer> entry : langStats.entrySet()) {
                totalStats.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }
    }

    private String fetchRepoUpdatedAt(String username, String repoName) {
        String url = "https://api.github.com/repos/" + username + "/" + repoName;
        ResponseEntity<Map<String, Object>> response =
                githubRestTemplate.exchange(url, HttpMethod.GET, buildAuthEntity(), new ParameterizedTypeReference<>() {});
        Map<String, Object> repoInfo = response.getBody();
        if (repoInfo != null && repoInfo.get("updated_at") != null) {
            return (String) repoInfo.get("updated_at");
        }
        return null;
    }

    private String fetchRepoReadme(String username, String repoName) {
        String readmeUrl = "https://api.github.com/repos/" + username + "/" + repoName + "/readme";
        try {
            Map<String, Object> readmeJson =
                    githubRestTemplate.exchange(readmeUrl, HttpMethod.GET, buildAuthEntity(), Map.class).getBody();

            if (readmeJson != null
                    && readmeJson.get("content") != null
                    && "base64".equals(readmeJson.get("encoding"))) {
                String base64Content = (String) readmeJson.get("content");
                return new String(Base64.getDecoder().decode(base64Content));
            }
        } catch (Exception e) {
            // 404 → fallback
        }

        // fallback: contents API로 README 탐색
        try {
            String contentsUrl = "https://api.github.com/repos/" + username + "/" + repoName + "/contents";
            ResponseEntity<List<Map<String, Object>>> response = githubRestTemplate.exchange(
                    contentsUrl,
                    HttpMethod.GET,
                    buildAuthEntity(),
                    new ParameterizedTypeReference<>() {}
            );

            List<Map<String, Object>> files = response.getBody();
            if (files != null) {
                for (Map<String, Object> file : files) {
                    String name = (String) file.get("name");
                    if (name != null && name.toLowerCase().startsWith("readme")) {
                        String downloadUrl = (String) file.get("download_url");
                        if (downloadUrl != null) {
                            return githubRestTemplate.exchange(downloadUrl, HttpMethod.GET, buildAuthEntity(), String.class).getBody();
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 무시
        }

        return "README 없음";
    }
}
