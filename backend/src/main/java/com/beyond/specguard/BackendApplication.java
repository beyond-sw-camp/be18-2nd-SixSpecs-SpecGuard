package com.beyond.specguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EntityScan(basePackages = "com.beyond.specguard")
@EnableJpaRepositories(basePackages = "com.beyond.specguard")
@org.springframework.boot.context.properties.ConfigurationPropertiesScan(
        basePackages = "com.beyond.specguard.common.properties"
)
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}
