package com.beyond.specguard.common.config;

import com.beyond.specguard.common.properties.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GitHubRestTemplateConfig {

    private final AppProperties appProperties;

    @Bean
    public RestTemplate githubRestTemplate(RestTemplateBuilder builder) {
        String githubToken = appProperties.getGithub().getToken();

        return builder
                .defaultHeader("Authorization", "token " + githubToken)
                .defaultHeader("Accept", "application/vnd.github.v3+json")
                .build();
    }
}
