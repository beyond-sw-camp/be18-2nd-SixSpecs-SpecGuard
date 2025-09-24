package com.beyond.specguard.event.listener;

import com.beyond.specguard.certificate.model.service.CertificateVerificationCodefService;
import com.beyond.specguard.event.ResumeSubmittedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateVerificationEventListener {

    private final CertificateVerificationCodefService verificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ResumeSubmittedEvent event) {
        UUID resumeId = event.resumeId();
        log.info("[CertificateVerificationEvent] AFTER_COMMIT: resumeId={}, thread={}",
                resumeId, Thread.currentThread().getName());
        verificationService.verifyCertificateAsync(resumeId);
    }
}
