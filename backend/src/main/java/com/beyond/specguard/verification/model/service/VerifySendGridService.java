package com.beyond.specguard.verification.model.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifySendGridService {

    @Value("${sendgrid.api.key}")
    private String apiKey;

    @Value("${verify.mail.from:${sendgrid.mail.from}}")
    private String fromEmail;

    @Value("${verify.mail.from-name:${sendgrid.mail.from-name}}")
    private String fromName;

    public void sendCodeEmail(String toEmail, String code, long ttlSeconds) throws IOException {
        Email from = new Email(fromEmail, fromName);
        Email to = new Email(toEmail);
        String subject = "[SpecGuard] 이메일 인증코드";
        String body = """
        <h3>이메일 인증코드</h3>
        <p>아래 코드를 입력하세요. 유효시간 %d초</p>
        <div style="font-size:24px;font-weight:700">%s</div>
        """.formatted(ttlSeconds, code);

        Content content = new Content("text/html", body);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(apiKey);
        Request req = new Request();
        try {
            req.setMethod(Method.POST);
            req.setEndpoint("mail/send");
            req.setBody(mail.build());
            sg.api(req);
        } catch (IOException e) {
            throw new RuntimeException("Verification 메일 발송 실패", e);
        }
        Response res = sg.api(req);
        log.info("SendGrid status={}, body={}", res.getStatusCode(), res.getBody());
    }

}
