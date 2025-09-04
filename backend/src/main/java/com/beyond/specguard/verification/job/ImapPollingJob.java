package com.beyond.specguard.verification.job;

import com.beyond.specguard.verification.dto.VerifyDto;
import com.beyond.specguard.verification.service.PhoneVerificationService;
import com.beyond.specguard.verification.util.ImapReader;
import jakarta.mail.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImapPollingJob {

    private final ImapReader imapReader;
    private final PhoneVerificationService verifyService;

    // 최근 처리한 UID (재처리 방지; 필요하면 Redis/DB에 저장)
    private volatile long lastUid = 0L;

    // 6자리 토큰 패턴
    private static final Pattern OTP = Pattern.compile("\\b([0-9]{6})\\b");

    // 2~3초 간격 권장 (지연 많으면 5초)
    @Scheduled(fixedDelayString = "${verify.imap.poll-ms:3000}")
    public void poll() {
        try {
            Message[] msgs = imapReader.fetchUnreadSinceUid(lastUid);
            for (Message m : msgs) {
                long uid = imapReader.uidOf(m);
                process(m);
                try {
                    imapReader.markSeen(m); // 처리 후 읽음 표시
                } catch (Exception ignore) {
                }
                if (uid > lastUid) lastUid = uid;
            }
        } catch (Exception e) {
            log.warn("IMAP poll error", e);
        }
    }

    // 개별 메일 처리: From에서 번호, 본문/첨부에서 OTP 추출 → finish() 호출
    private void process(Message m) {
        try {
            String phone = imapReader.extractPhone(m);
            if (phone.isEmpty()) {
                String fromLog = imapReader.fromString(m);
                log.info("skip (no phone) from={}", fromLog);
                return;
            }

            String text = imapReader.extractAllText(m);
            if (text == null) text = "";

            // OTP 추출 보강: '인증번호 : 123456' 패턴 우선, 실패 시 6자리 백업
            String token = null;
            Matcher m1 = Pattern.compile("(?:인증\\s*번호|인증번호|OTP|코드)\\s*[:：]\\s*(\\d{6})").matcher(text);
            if (m1.find()) {
                token = m1.group(1);
            } else {
                Matcher m2 = OTP.matcher(text); // 기존 6자리 백업
                if (m2.find()) token = m2.group(1);
            }

            if (token == null) {
                log.info("skip (no OTP) subj={}", m.getSubject());
                return;
            }

            try {
                verifyService.finish(new VerifyDto.VerifyFinishRequest(token, phone));
                log.info("verified via IMAP: phone={}, token={}", phone, token);
            } catch (Exception ex) {
                log.warn("finish() failed. phone={}, token={}, subj={}",
                        phone, token, m.getSubject(), ex);
            }
        } catch (Exception e) {
            log.warn("process mail failed", e);
        }
    }
}
