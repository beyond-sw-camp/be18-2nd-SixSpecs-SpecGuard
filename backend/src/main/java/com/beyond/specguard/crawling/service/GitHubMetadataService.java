package com.beyond.specguard.crawling.service;

import com.beyond.specguard.common.exception.errorcode.ErrorCode;
import com.beyond.specguard.crawling.dto.GitMetadataResponse;
import com.beyond.specguard.crawling.entity.GitHubMetadata;
import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.crawling.repository.GithubMetadataRepository;
import com.beyond.specguard.githubcrawling.exception.errorcode.GitErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GitHubMetadataService {

    private final GithubMetadataRepository gitHubMetadataRepository;

    public GitMetadataResponse getLanguageStatsPercentage(UUID resumeId) {
        GitHubMetadata metadata = gitHubMetadataRepository.findByResumeId(resumeId)
                .orElseThrow(() -> new CustomException(GitErrorCode.GITHUB_API_ERROR));

        Map<String, Integer> stats = metadata.getLanguageStats();
        int total = stats.values().stream().mapToInt(Integer::intValue).sum();

        Map<String, Double> percentages = stats.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> total == 0 ? 0.0 : Math.round((e.getValue() * 1000.0) / total) / 10.0
                ));

        return GitMetadataResponse.builder()
                .languagePercentages(percentages)
                .createdAt(metadata.getCreatedAt())
                .build();
    }
}
