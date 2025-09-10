package com.github.github.controller;

import com.github.github.dto.GitHubStatsDto;
import com.github.github.service.GitHubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GitHubController {

    private final GitHubService gitHubService;

    // GET /api/github/analyze?url=https://github.com/torvalds
    @GetMapping("/analyze")
    public ResponseEntity<GitHubStatsDto> analyzeGitHubUser(@RequestParam String url) {
        GitHubStatsDto stats = gitHubService.analyzeGitHubUrl(url);
        return ResponseEntity.ok(stats);
    }
}
