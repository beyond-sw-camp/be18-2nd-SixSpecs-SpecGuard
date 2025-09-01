package com.beyond.specguard.certificate.model.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class EasyCodefClientInfo {
    @Value("${codef.public_key}")
    private String publicKey;

    @Value("${codef.demo_client_id}")
    private String demoClientId;

    @Value("${codef.demo_client_secret}")
    private String demoClientSecret;
}
