package com.beyond.specguard.certificate.model.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class EasyCodefClientInfo {
    @Value("${codef.public_key}")
    public String PUBLIC_KEY;

    @Value("${codef.demo_client_id}")
    public String DEMO_CLIENT_ID;

    @Value("${codef.demo_client_secret}")
    public String DEMO_CLIENT_SECRET;
}
