package com.beyond.specguard.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient pythonWebClient(WebClient.Builder builder){
        return builder
                .baseUrl("http://localhost:8000/api/v1")
                .build();
    }
}
