package com.beyond.specguard.notioncrawling.service;

import com.beyond.specguard.crawling.entity.CrawlingResult;
import com.beyond.specguard.crawling.repository.CrawlingResultRepository;
import com.beyond.specguard.notioncrawling.dto.NotionPageDto;
import com.beyond.specguard.resume.model.repository.ResumeLinkRepository;
import com.beyond.specguard.resume.model.repository.ResumeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicNotionCrawlerService {

    private final CrawlingResultRepository crawlingResultRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeLinkRepository resumeLinkRepository;
    private final ObjectMapper objectMapper;

    /**
     * resumeId와 resumeLinkId, 노션 URL을 받아
     * Selenium 기반 크롤링 + JSON 직렬화 + GZIP 압축 후 CrawlingResult에 저장
     */
    @Transactional
    public void crawlAndUpdate(UUID resultId, String notionUrl) {
        CrawlingResult result = crawlingResultRepository.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("CrawlingResult not found: " + resultId));

        // ----------------------------
        // URL null/빈값 체크
        // ----------------------------
        if (notionUrl == null || notionUrl.trim().isEmpty()) {
            result.updateStatus(CrawlingResult.CrawlingStatus.NOTEXISTED);
            crawlingResultRepository.save(result);
            log.warn("URL 없음 - resultId={}, url={}", resultId, notionUrl);
            return;
        }

        // ----------------------------
        // Selenium WebDriver 설정
        // ----------------------------
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // 화면 없이 실행
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);

        // ----------------------------
        // 외부 try 제거됨 (주석 처리)
        // ----------------------------
        // try {
        try {
            driver.get(notionUrl);

            // 페이지 로딩 대기 (간단하게 Thread.sleep)
            Thread.sleep(3000);

            // ----------------------------
            // 페이지 파싱
            // ----------------------------
            String title = driver.getTitle();

            // 전체 페이지의 텍스트 수집
            WebElement contentElement = driver.findElement(By.cssSelector("div.notion-page-content"));
            String content = contentElement.getText();

            // 코드 블록
            List<WebElement> codeElements = driver.findElements(By.cssSelector("pre code"));
            List<String> codeBlocks = codeElements.stream()
                    .map(WebElement::getText)
                    .collect(Collectors.toList());

            // 태그
            List<WebElement> tagElements = driver.findElements(By.cssSelector(".notion-pill"));
            List<String> tags = tagElements.stream()
                    .map(WebElement::getText)
                    .collect(Collectors.toList());

            NotionPageDto pageDto = new NotionPageDto(notionUrl, title, content, codeBlocks, tags);


            // ----------------------------
            // 압축 전 DTO 출력
            // ----------------------------
            System.out.println("====== 압축 전 NotionPageDto ======");
            System.out.println(pageDto.toString());
            System.out.println("===================================");


            // ----------------------------
            // JSON 직렬화 + GZIP 압축
            // ----------------------------
            String serialized = objectMapper.writeValueAsString(pageDto);

            byte[] compressed;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
                gzipOut.write(serialized.getBytes(StandardCharsets.UTF_8));
                gzipOut.finish();
                compressed = baos.toByteArray();
            }

            // ----------------------------
            // CrawlingResult 업데이트
            // ----------------------------
            result.updateContents(compressed);
            result.updateStatus(CrawlingResult.CrawlingStatus.COMPLETED);
            crawlingResultRepository.save(result);

            log.info("Notion 크롤링 완료 - resultId={}, url={}", resultId, notionUrl);

        } catch (Exception e) {
            result.updateStatus(CrawlingResult.CrawlingStatus.FAILED);
            crawlingResultRepository.save(result);

            log.error("크롤링 실패 - resultId={}, url={}, error={}", resultId, notionUrl, e.getMessage(), e);
        } finally {
            driver.quit();
        }
        // ----------------------------
        // 외부 try 제거됨 (주석 처리)
        // ----------------------------
        // } catch (Exception e) {
        //     throw new RuntimeException(e);
        // }
    }

//    /**
//     * DB에서 압축된 데이터 꺼내서 NotionPageDto로 복원 후 터미널 출력
//     */
//    public void printDecodedDto(UUID resultId) {
//        CrawlingResult result = crawlingResultRepository.findById(resultId)
//                .orElseThrow(() -> new IllegalArgumentException("CrawlingResult not found: " + resultId));
//
//        byte[] compressed = result.getContents();
//        if (compressed == null || compressed.length == 0) {
//            System.out.println("저장된 contents 없음");
//            return;
//        }
//
//        try (ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
//             GZIPInputStream gzipIn = new GZIPInputStream(bais);
//             InputStreamReader isr = new InputStreamReader(gzipIn, StandardCharsets.UTF_8);
//             BufferedReader reader = new BufferedReader(isr)) {
//
//            StringBuilder sb = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                sb.append(line);
//            }
//
//            NotionPageDto dto = objectMapper.readValue(sb.toString(), NotionPageDto.class);
//
//            System.out.println("====== 복원된 NotionPageDto ======");
//            System.out.println("URL: " + dto.getUrl());
//            System.out.println("Title: " + dto.getTitle());
//            System.out.println("Content: " + dto.getContent());
//            System.out.println("CodeBlocks: " + dto.getCodeBlocks());
//            System.out.println("Tags: " + dto.getTags());
//            System.out.println("================================");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException("압축 해제 및 출력 실패", e);
//        }
//    }

}