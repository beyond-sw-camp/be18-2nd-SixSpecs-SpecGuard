package com.beyond.specguard.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {
    @Bean("specguardMailSender") // 기본 메일러와 구분
    public JavaMailSender specguardMailSender(
            @Value("${specguard.mail.host}") String host,
            @Value("${specguard.mail.port}") int port,
            @Value("${specguard.mail.username}") String username,
            @Value("${specguard.mail.password}") String password,
            @Value("${specguard.mail.auth:true}") boolean auth,
            @Value("${specguard.mail.starttls:true}") boolean starttls,
            @Value("${specguard.mail.debug:false}") boolean debug
    ) {
        var s = new JavaMailSenderImpl();
        s.setHost(host);
        s.setPort(port);
        s.setUsername(username);
        s.setPassword(password);

        Properties p = s.getJavaMailProperties();
        p.put("mail.transport.protocol", "smtp");
        p.put("mail.smtp.auth", String.valueOf(auth));
        p.put("mail.smtp.starttls.enable", String.valueOf(starttls));
        p.put("mail.debug", String.valueOf(debug));
        return s;
    }
}
