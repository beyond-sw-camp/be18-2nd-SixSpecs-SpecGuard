package com.beyond.specguard.event.client;

import com.beyond.specguard.event.dto.KeywordRequest;
import com.beyond.specguard.event.dto.KeywordResponse;
import com.beyond.specguard.event.dto.SummaryRequest;
import com.beyond.specguard.event.dto.SummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class TemplateNlpClient {

    private final WebClient pythonWebClient;

    public SummaryResponse summarize(SummaryRequest request) {
        return pythonWebClient.post()
                .uri("/nlp/summary")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(SummaryResponse.class)
                .block();
    }

    public KeywordResponse extractKeywords(KeywordRequest request) {
        return pythonWebClient.post()
                .uri("/nlp/keywords")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(KeywordResponse.class)
                .block();
    }
}
