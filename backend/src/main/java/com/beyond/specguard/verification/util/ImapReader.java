package com.beyond.specguard.verification.util;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.search.FlagTerm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class ImapReader {

    @Value("${imap.host}")     private String host;
    @Value("${imap.port:993}") private int port;
    @Value("${imap.username}") private String username;
    @Value("${imap.password}") private String password;
    @Value("${imap.folder:INBOX}") private String folder;
    @Value("${imap.recent-window:120}") private int recentWindow; // 최근 N개만 스캔

    private Properties props;
    private Session session;
    private Store store;
    private Folder inbox; // Gmail이면 com.sun.mail.imap.IMAPFolder로 캐스팅 가능(UID 지원)

    private static final Pattern PHONE_BLOCK =
            Pattern.compile("(\\+82\\d{9,10}|0\\d{9,10}|\\b\\d{10,11}\\b)");

    private static String normalizeKrPhone(String raw) {
        if (raw == null) return "";
        String s = raw.replaceAll("[^0-9+]", "");
        if (s.startsWith("+82")) s = "0" + s.substring(3);   // +8210xxxx -> 010xxxx
        s = s.replaceAll("\\D", "");
        if (s.length() < 10 || s.length() > 11) return "";
        if (!s.startsWith("0")) s = "0" + s; // 안전망
        return s;
    }

    public String extractPhone(Message m) throws Exception {
        Address[] arr = m.getFrom();
        if (arr == null || arr.length == 0) return "";
        Address a = arr[0];
        if (a instanceof InternetAddress ia) {
            // 1) 표시명(personal)에서 먼저 시도
            String personal = ia.getPersonal();
            if (personal != null) {
                Matcher pm = PHONE_BLOCK.matcher(personal);
                if (pm.find()) {
                    String n = normalizeKrPhone(pm.group(1));
                    if (!n.isEmpty()) return n;
                }
            }
            // 2) 이메일 주소의 로컬파트( @ 앞 )에서 시도
            String email = ia.getAddress(); // ex) 01034696728@ktfmms.magicn.com
            if (email != null) {
                int at = email.indexOf('@');
                String local = (at > 0) ? email.substring(0, at) : email;
                Matcher em = PHONE_BLOCK.matcher(local);
                if (em.find()) {
                    String n = normalizeKrPhone(em.group(1));
                    if (!n.isEmpty()) return n;
                }
            }
        }
        // 3) 실패 시 전체 문자열에서라도 마지막 시도 (방어적)
        String fromStr = fromString(m);
        Matcher any = PHONE_BLOCK.matcher(fromStr);
        if (any.find()) return normalizeKrPhone(any.group(1));
        return "";
    }

    @PostConstruct
    void open() throws Exception {
        props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.connectiontimeout", "10000");
        props.put("mail.imaps.timeout", "10000");
        props.put("mail.imaps.ssl.enable", "true");

        session = Session.getInstance(props, null);
        // session.setDebug(true);
        store = session.getStore("imaps");
        store.connect(host, port, username, password);

        inbox = store.getFolder(folder);
        inbox.open(Folder.READ_WRITE); // 처리 후 SEEN 플래그 세팅을 위해 READ_WRITE
        log.info("IMAP connected. folder={}, recentWindow={}", folder, recentWindow);
    }

    @PreDestroy
    public void close() {
        try { if (inbox != null && inbox.isOpen()) inbox.close(false); } catch (Exception ignore) {}
        try { if (store != null && store.isConnected()) store.close(); } catch (Exception ignore) {}
    }

    private void ensureOpen() throws MessagingException {
        if (inbox == null || !inbox.isOpen()) {
            if (store == null || !store.isConnected()) {
                throw new MessagingException("IMAP store not connected");
            }
            inbox = store.getFolder(folder);
            inbox.open(Folder.READ_WRITE);
        }
    }

    /** ① 마지막 UID 이후 + 미열람만(최근 N개 범위) */
    public Message[] fetchUnreadSinceUid(long lastUid) throws Exception {
        ensureOpen();

        // 전체에서 최근 N개 메시지만 대상으로 하여 속도 확보
        int total = inbox.getMessageCount();
        if (total <= 0) return new Message[0];
        int fromMsg = Math.max(1, total - recentWindow + 1);
        Message[] allRecent = inbox.getMessages(fromMsg, total);

        // 메타데이터 프리패치
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.ENVELOPE);
        fp.add(FetchProfile.Item.CONTENT_INFO);
        inbox.fetch(allRecent, fp);

        // 미열람 + UID>lastUid 만 반환
        return Arrays.stream(allRecent)
                .filter(m -> {
                    try {
                        return !m.isSet(Flags.Flag.SEEN) && uidOf(m) > lastUid;
                    } catch (Exception e) { return false; }
                })
                .toArray(Message[]::new);
    }

    /** ② 미열람 전체(최근 N개로 슬라이스) — 필요시 사용 */
    public Message[] fetchUnread() throws Exception {
        ensureOpen();
        // UNSEEN만 먼저 찾고, 그중 최근 N개만 슬라이스
        Message[] unread = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
        if (unread.length == 0) return unread;
        int from = Math.max(0, unread.length - recentWindow);
        Message[] slice = Arrays.copyOfRange(unread, from, unread.length);

        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.ENVELOPE);
        fp.add(FetchProfile.Item.CONTENT_INFO);
        inbox.fetch(slice, fp);
        return slice;
    }

    /** ③ 개별 메시지 UID (IMAPFolder 필요) */
    public long uidOf(Message m) throws MessagingException {
        ensureOpen();
        if (inbox instanceof com.sun.mail.imap.IMAPFolder imapFolder) {
            return imapFolder.getUID(m);
        }
        // UID 미지원 서버 대비: fallback (비권장, 항상 0)
        return 0L;
    }

    /** ④ 처리 후 읽음 표시 */
    public void markSeen(Message m) throws MessagingException {
        m.setFlag(Flags.Flag.SEEN, true);
    }

    /** ⑤ 제목/본문/첨부(txt)에서 토큰 포함 여부 — 빠른 검사용 */
    public boolean containsToken(Message m, String token) throws Exception {
        // 제목 먼저(속도↑, 인코딩 이슈↓)
        String subj = Optional.ofNullable(m.getSubject()).orElse("");
        if (subj.contains(token)) return true;

        String text = extractAllText(m);
        return text != null && text.contains(token);
    }

    /** ⑥ From을 문자열로 풀기(번호 추출 전 단계) */
    public String fromString(Message m) throws Exception {
        Address[] arr = m.getFrom();
        if (arr == null || arr.length == 0) return "";
        Address a = arr[0];
        if (a instanceof InternetAddress ia) {
            String personal = ia.getPersonal();
            String email = ia.getAddress();
            return (personal != null ? personal + " " : "") + (email != null ? email : "");
        }
        return a.toString();
    }

    /** ⑦ 제목 + 모든 텍스트 파트(본문/첨부)를 합쳐 문자열로 */
    public String extractAllText(Part p) throws Exception {
        if (p.isMimeType("text/plain")) {
            Object c = p.getContent();
            return c instanceof String ? (String) c : null;
        }
        if (p.isMimeType("text/html")) {
            String html = (String) p.getContent();
            // 태그 제거 (아주 러프하게)
            return html.replaceAll("<[^>]+>", " ");
        }
        if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();

            // 1) text/plain 우선 수집
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    String s = extractAllText(bp);
                    if (s != null) sb.append(s).append('\n');
                }
            }
            // 2) text/html 수집
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/html")) {
                    String s = extractAllText(bp);
                    if (s != null) sb.append(s).append('\n');
                }
            }
            // 3) 첨부 txt 수집 (Gmail이 text_0.txt로 붙이는 케이스)
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bp = mp.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(bp.getDisposition())
                        && bp.getFileName() != null
                        && bp.getFileName().toLowerCase().endsWith(".txt")) {
                    try (InputStream is = bp.getInputStream()) {
                        sb.append(new String(is.readAllBytes(), StandardCharsets.UTF_8)).append('\n');
                    }
                }
            }
            return sb.toString();
        }
        if (p.isMimeType("message/rfc822")) {
            return extractAllText((Part) p.getContent());
        }
        return null;
    }

    /** ⑧ 기존 findToken 보완: 최근 N개에서 토큰 포함 메일 1건 찾아 From/Message-ID 반환 */
    public Optional<ImapMatch> findToken(String token) {
        try {
            ensureOpen();
            int total = inbox.getMessageCount();
            if (total <= 0) return Optional.empty();
            int fromMsg = Math.max(1, total - recentWindow + 1);
            Message[] msgs = inbox.getMessages(fromMsg, total);

            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add(FetchProfile.Item.CONTENT_INFO);
            inbox.fetch(msgs, fp);

            for (int i = msgs.length - 1; i >= 0; i--) {
                Message m = msgs[i];
                if (!containsToken(m, token)) continue;

                String from = fromString(m);
                String[] ids = m.getHeader("Message-ID");
                String messageId = (ids != null && ids.length > 0) ? ids[0] : null;
                return Optional.of(new ImapMatch(from, messageId));
            }
            return Optional.empty();
        } catch (Exception e) {
            log.warn("IMAP findToken failed: {}", e.toString(), e);
            return Optional.empty();
        }
    }

    public record ImapMatch(String from, String messageId) {}
}
