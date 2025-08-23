package com.beyond.specguard.company.invite.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SendGridService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.mail.from}")
    private String fromEmail;

    @Value("${sendgrid.mail.from-name}")
    private String fromName;

    @Value("${invite.base-url}")
    private String inviteBaseUrl;

    public void sendInviteEmail(String toEmail, String token) {
        Email from = new Email(fromEmail, fromName);
        String subject = "[SpecGuard] 초대 메일이 도착했습니다!";
        Email to = new Email(toEmail);

        String inviteUrl = inviteBaseUrl + "?token=" + token;

        String contentValue = "<h3>SpecGuard 초대 메일</h3>"
                + "<p>아래 링크를 클릭하여 초대를 수락하세요:</p>"
                + "<a href=\"" + inviteUrl + "\">초대 수락하기</a>";

        Content content = new Content("text/html", contentValue);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            System.out.println("✅ SendGrid Response Status: " + response.getStatusCode());
        } catch (IOException ex) {
            throw new RuntimeException("이메일 발송 중 오류 발생", ex);
        }
    }
}
