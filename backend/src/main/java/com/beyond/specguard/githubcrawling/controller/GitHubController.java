/*
package com.beyond.specguard.githubcrawling.controller;


import com.beyond.specguard.githubcrawling.model.dto.GitHubStatsDto;
import com.beyond.specguard.githubcrawling.model.service.GitHubService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GitHubController {

    private final GitHubService gitHubService;
    private final ObjectMapper objectMapper; // JSON 직렬화용

    // GET /api/github/analyze?url=https://github.com/torvalds
    @GetMapping("/analyze")
    public ResponseEntity<byte[]> analyzeGitHubUser(@RequestParam String url) throws IOException {
        GitHubStatsDto stats = gitHubService.analyzeGitHubUrl(url);

        // DTO -> JSON
        byte[] jsonBytes = objectMapper.writeValueAsBytes(stats);

        // JSON -> GZIP 압축
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            gzipOut.write(jsonBytes);
        }
        byte[] gzipBytes = baos.toByteArray();

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_ENCODING, "gzip");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

        return new ResponseEntity<>(gzipBytes, headers, HttpStatus.OK);
    }
}
*/
