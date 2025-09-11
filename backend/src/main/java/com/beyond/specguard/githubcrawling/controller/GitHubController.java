package com.beyond.specguard.githubcrawling.controller;

import com.beyond.specguard.githubcrawling.model.dto.GitHubStatsDto;
import com.beyond.specguard.githubcrawling.model.service.GitHubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/github")
@RequiredArgsConstructor
public class GitHubController {

    private final GitHubService gitHubService;

    // GET /api/github/analyze?url=https://github.com/torvalds
    @GetMapping("/analyze")
    public ResponseEntity<GitHubStatsDto> analyzeGitHubUser(
            @RequestParam String url
    ) {
        GitHubStatsDto stats = gitHubService.analyzeGitHubUrl(url);
        return ResponseEntity.ok(stats);
    }
}
