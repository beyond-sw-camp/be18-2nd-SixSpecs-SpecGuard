package com.beyond.specguard.githubcrawling.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class GitHubStatsDto {
    private int repositoryCount;
    private int commitCount;
    private Map<String, Integer> languageStats;
    private Map<String, String> repoUpdatedAt;
    private Map<String, String> repoReadme;

}
