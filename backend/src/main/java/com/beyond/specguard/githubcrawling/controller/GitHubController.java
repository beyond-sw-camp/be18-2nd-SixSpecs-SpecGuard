//package com.beyond.specguard.githubcrawling.controller;
//
//import com.beyond.specguard.githubcrawling.model.dto.GitHubStatsDto;
//import com.beyond.specguard.githubcrawling.model.service.GitHubService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//import com.beyond.specguard.crawling.entity.CrawlingResult;
//
//
//
//import java.util.UUID;
//
//@RestController
//@RequiredArgsConstructor
//public class GitHubController {
//
//    private final GitHubService gitHubService;
//
//    /**
//     * 하드코딩된 resumeId와 URL로 GitHubService 실행
//     */
//    @GetMapping("/test/github")
//    public GitHubStatsDto testGitHubCrawling() {
//        // 하드코딩 resumeId (UUID 형태)
//        UUID resumeId = UUID.fromString("6fb3eca7-f405-4cdd-8ccc-c6fdfdb03e90");//
//
//        // TODO: 실제 URL로 바꿔서 테스트 가능
//        String url = "https://github.com/sumgo-ssri";
//
//        // CrawlingResult에 URL 세팅은 테스트용이므로, 기존 GitHubService 내부 로직에 따라
//        // 실제로 CrawlingResultRepository에서 resumeId로 조회되는 객체가 있어야 합니다.
//        // 테스트 목적으로는 DB에 미리 CrawlingResult 더미 데이터를 넣어두거나,
//        // 서비스 코드를 조금 수정해서 하드코딩 URL 전달 가능
//
//        return gitHubService.analyzeGitHubUrl(resumeId);
//    }
//}

