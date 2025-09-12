package com.beyond.specguard.verification.model.service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyMailService {

    @Qualifier("specguardMailSender")
    private final JavaMailSender mail;

    @Value("${sendgrid.api.key}")
    private String apiKey;

    @Value("${sendgrid.mail.from}")
    private String fromEmail;

    @Value("${sendgrid.mail.from-name:SpecGuard}")
    private String fromName;

    public void sendText(String to, String subject, String body) {
        var msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        mail.send(msg);
    }

    public void sendHtml(String to, String subject, String html) throws MessagingException {
        var mime = mail.createMimeMessage();
        var helper = new MimeMessageHelper(mime, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mail.send(mime);
    }

}
