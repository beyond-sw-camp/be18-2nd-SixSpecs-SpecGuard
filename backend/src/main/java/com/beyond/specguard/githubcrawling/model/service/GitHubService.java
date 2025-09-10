package com.beyond.specguard.githubcrawling.model.service;

import com.beyond.specguard.githubcrawling.model.dto.GitHubStatsDto;
import com.beyond.specguard.githubcrawling.util.GitHubUrlParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitHubService {

    private final GitHubApiClient gitHubApiClient;

    public GitHubStatsDto analyzeGitHubUrl(String url) {
        // 1. 사용자 URL에서 username 추출
        String username = GitHubUrlParser.extractUsername(url);

        // 2. GitHub API 클라이언트에게 통계 요청
        return gitHubApiClient.fetchGitHubStats(username);
    }
}
