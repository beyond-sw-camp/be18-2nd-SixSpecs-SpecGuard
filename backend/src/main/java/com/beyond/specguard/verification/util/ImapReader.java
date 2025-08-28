package com.beyond.specguard.verification.util;

import jakarta.mail.Address;
import jakarta.mail.Folder;
import jakarta.annotation.PostConstruct;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

@Component
@Slf4j
public class ImapReader {

    @Value("${imap.host}") private String host;
    @Value("${imap.port}") private int port;
    @Value("${imap.username}") private String username;
    @Value("${imap.password}") private String password;
    @Value("${imap.folder:INBOX}") private String folder;

    private Properties props;

    @PostConstruct
    void init() {
        props = new Properties();
        props.put("mail.store.protocol", "imaps");
        // 타임아웃
        props.put("mail.imaps.connectiontimeout", "10000");
        props.put("mail.imaps.timeout", "10000");
        props.put("mail.imaps.ssl.enable", "true"); // imaps면 보통 기본이나 명시해도 무방
    }

    public Optional<ImapMatch> findToken(String token) {
        Store store = null;
        Folder inbox = null;
        try {
            Session session = Session.getInstance(props, null);
            // session.setDebug(true); // 필요시 디버그
            store = session.getStore("imaps");
            store.connect(host, port, username, password);

            inbox = store.getFolder(folder);
            inbox.open(Folder.READ_ONLY);

            int count = inbox.getMessageCount();
            if (count == 0) return Optional.empty();

            Message[] messages = inbox.getMessages(Math.max(1, count - 50), count);
            for (int i = messages.length - 1; i >= 0; i--) {
                String body = getText(messages[i]);
                if (body != null && body.contains(token)) {
                    Address[] fromArr = messages[i].getFrom();
                    String from = (fromArr != null && fromArr.length > 0) ? fromArr[0].toString() : "";

                    String[] ids = messages[i].getHeader("Message-ID");
                    String messageId = (ids != null && ids.length > 0) ? ids[0] : null;

                    return Optional.of(new ImapMatch(from, messageId));
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            log.warn("IMAP read failed: {}", e.toString(), e);
            return Optional.empty();
        } finally {
            try { if (inbox != null && inbox.isOpen()) inbox.close(false); } catch (Exception ignore) {}
            try { if (store != null && store.isConnected()) store.close(); } catch (Exception ignore) {}
        }
    }

    private String getText(Part p) throws Exception {
        if (p.isMimeType("text/*")) {
            Object c = p.getContent();
            return (c instanceof String) ? (String) c : null;
        }
        if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            // text/plain 우선 탐색 후 없으면 첫 파트 반환(개선)
            String candidate = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                String s = getText(bp);
                if (s == null) continue;
                if (bp.isMimeType("text/plain")) return s; // 최우선
                if (candidate == null) candidate = s;      // fallback (html 등)
            }
            return candidate;
        }
        return null;
    }

    public record ImapMatch(String from, String messageId) {}
}
