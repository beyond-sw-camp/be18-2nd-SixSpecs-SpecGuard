package com.beyond.specguard.event.listener;

import com.beyond.specguard.event.ResumeSubmittedEvent;
import com.beyond.specguard.resume.model.entity.Resume;
import com.beyond.specguard.resume.model.entity.ResumeLink;
import com.beyond.specguard.resume.model.repository.ResumeLinkRepository;
import com.beyond.specguard.resume.model.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResumeSubmittedListener {

    private final ResumeLinkRepository resumeLinkRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeLinkProcessor resumeLinkProcessor;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleResumeSubmitted(ResumeSubmittedEvent event) {
        UUID resumeId = event.resumeId();
        log.info("[ResumeSubmittedEvent] AFTER_COMMIT: resumeId={}, thread={}",
                resumeId, Thread.currentThread().getName());

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalStateException("Resume not found: " + resumeId));

        List<ResumeLink> links = resumeLinkRepository.findByResume_Id(resumeId);

        //  각각 비동기로 실행
        for (ResumeLink link : links) {
            resumeLinkProcessor.processLinkAsync(resume, link);
        }
    }
}
