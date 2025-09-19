package com.beyond.specguard.event.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PythonCrawlerClient {
    private final WebClient pythonWebClient;

    public Map<String, Object> callVelogApi(UUID resumeId, String url) {
        String endpoint = String.format("/ingest/resumes/%s/velog/start", resumeId);

        return pythonWebClient.post()
                .uri(endpoint)
                .bodyValue(Map.of("url", url))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }

}
