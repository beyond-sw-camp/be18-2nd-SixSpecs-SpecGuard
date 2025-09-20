package com.beyond.specguard.notioncrawling.service;

import com.beyond.specguard.crawling.entity.CrawlingResult;
import com.beyond.specguard.crawling.repository.CrawlingResultRepository;
import com.beyond.specguard.notioncrawling.dto.NotionPageDto;
import com.beyond.specguard.resume.model.entity.Resume;
import com.beyond.specguard.resume.model.entity.ResumeLink;
import com.beyond.specguard.resume.model.repository.ResumeLinkRepository;
import com.beyond.specguard.resume.model.repository.ResumeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
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
     * 크롤링 + JSON 직렬화 + GZIP 압축 후 CrawlingResult에 저장
     * 실패 시 CrawlingStatus.FAILED로 업데이트하고 로그 기록
     */
    @Transactional
    public void crawlAndUpdate(UUID resultId, String notionUrl) {
        CrawlingResult result = crawlingResultRepository.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("CrawlingResult not found: " + resultId));

        try {
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
            // Notion 페이지 크롤링
            // ----------------------------
            Document doc;
            try {
                doc = Jsoup.connect(notionUrl).get();
            } catch (IOException e) {
                // 네트워크 단에서 아예 연결 불가 → NOTEXISTED
                result.updateStatus(CrawlingResult.CrawlingStatus.NOTEXISTED);
                crawlingResultRepository.save(result);
                log.warn("URL 존재하지 않음 - resultId={}, url={}", resultId, notionUrl);
                return;
            }

            // ----------------------------
            // 페이지 파싱
            // ----------------------------
            String title = doc.title();
            String content = doc.select("p").text();

            List<String> codeBlocks = doc.select("pre code").stream()
                    .map(Element::text)
                    .collect(Collectors.toList());

            List<String> tags = doc.select(".notion-pill").stream()
                    .map(Element::text)
                    .collect(Collectors.toList());

            NotionPageDto pageDto = new NotionPageDto(notionUrl, title, content, codeBlocks, tags);

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
        }
    }
}