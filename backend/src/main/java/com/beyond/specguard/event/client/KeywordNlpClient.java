package com.beyond.specguard.event.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class KeywordNlpClient {
    private final WebClient pythonWebClient;

    public void extractKeywords(UUID resumeId){
        try{
            pythonWebClient.post()
                    .uri("/nlp/crawlingKeyword")
                    .bodyValue(new StartBody(resumeId.toString()))
                    .retrieve()
                    .bodyToMono(Void.class) // 응답 바디는 안 써도 되니까 Void
                    .block();

            log.info("NLP 키워드 추출 요청 완료 - resumeId={}", resumeId);
        } catch (Exception e) {
            log.error("NLP 키워드 추출 요청 실패 - resumeId={}, error={}", resumeId, e.getMessage(), e);
        }

    }
    private record StartBody(String resumeId) {}
}
