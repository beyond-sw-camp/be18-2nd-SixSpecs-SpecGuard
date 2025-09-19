/*
package com.beyond.specguard.notioncrawling.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/notion")
public class NotionCrawlController {
    private final PublicNotionCrawlerService crawlerService;

    public NotionCrawlController(PublicNotionCrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    @GetMapping("/crawl-compressed")
    public ResponseEntity<byte[]> crawlCompressed(@RequestParam String url) throws IOException {
        // 서비스에서 이미 GZIP 압축된 데이터 반환
        byte[] compressedData = crawlerService.crawlPageAndCompress(url);

        return ResponseEntity.ok()
                .header("Content-Encoding", "gzip")               // 압축임을 명시
                .header("Content-Disposition", "attachment; filename=\"notion_page.gz\"") // Postman/브라우저 다운로드용
                .header("Content-Type", "application/octet-stream") // JSON이 아닌 바이너리
                .body(compressedData);
    }

}  */
