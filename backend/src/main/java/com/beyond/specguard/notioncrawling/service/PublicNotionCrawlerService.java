package com.beyond.specguard.notioncrawling.service;

import com.beyond.specguard.crawling.entity.CrawlingResult;
import com.beyond.specguard.crawling.repository.CrawlingResultRepository;
import com.beyond.specguard.notioncrawling.dto.NotionPageDto;
import com.beyond.specguard.resume.model.entity.core.Resume;
import com.beyond.specguard.resume.model.entity.core.ResumeLink;
import com.beyond.specguard.resume.model.repository.ResumeLinkRepository;
import com.beyond.specguard.resume.model.repository.ResumeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
    public UUID crawlAndSaveWithLogging(UUID resumeId, UUID resumeLinkId, String notionUrl) throws IOException {
        // 1️⃣ Resume & ResumeLink 조회
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found: " + resumeId));

        ResumeLink resumeLink = resumeLinkRepository.findById(resumeLinkId)
                .orElseThrow(() -> new IllegalArgumentException("ResumeLink not found: " + resumeLinkId));

        CrawlingResult result = CrawlingResult.builder()
                .resume(resume)
                .resumeLink(resumeLink)
                .crawlingStatus(CrawlingResult.CrawlingStatus.RUNNING)
                .build();

        crawlingResultRepository.save(result);

        try {
            // ----------------------------
            // 2️⃣ Notion 페이지 크롤링
            // ----------------------------
            Document doc;
            try {
                doc = Jsoup.connect(notionUrl).get();
            } catch (IOException e) {
                // URL 존재하지 않음 → NOTEXISTED 상태 저장
                result.updateStatus(CrawlingResult.CrawlingStatus.NOTEXSITED);
                log.warn("URL 존재하지 않음 - resumeId={}, url={}", resumeId, notionUrl);
                return result.getId(); // 예외 던지지 않고 반환
            }

            // ----------------------------
            // 3️⃣ 페이지 파싱
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
            // 3️⃣ JSON 직렬화 + GZIP 압축
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
            // 4️⃣ CrawlingResult 업데이트
            // ----------------------------
            result.updateContents(compressed);
            result.updateStatus(CrawlingResult.CrawlingStatus.COMPLETED);

            crawlingResultRepository.save(result);
            log.info("크롤링 완료 - resumeId={}, url={}", resumeId, notionUrl);

            return result.getId();

        } catch (Exception e) {
            // ----------------------------
            // 실패 시 상태 업데이트 및 로그
            // ----------------------------
            result.updateStatus(CrawlingResult.CrawlingStatus.FAILED);
            crawlingResultRepository.save(result);

            log.error("크롤링 실패 - resumeId={}, url={}, error={}", resumeId, notionUrl, e.getMessage(), e);
            throw e; // 필요하면 커스텀 예외로 래핑 가능

        } finally {
            // 최종 상태 저장
            crawlingResultRepository.save(result);
        }
    }
}