package com.github.github.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Value("{GITHUB_TOKEN}")
    private String githubToken;

    @Bean
    public RestTemplate githubRestTemplate(RestTemplateBuilder builder) {
        return builder
                .defaultHeader("Authorization", "token " + githubToken)
                .defaultHeader("Accept", "application/vnd.github.v3+json")
                .build();
    }
}
