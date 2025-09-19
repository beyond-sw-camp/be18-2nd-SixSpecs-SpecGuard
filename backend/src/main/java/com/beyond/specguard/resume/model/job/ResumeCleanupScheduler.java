package com.beyond.specguard.resume.model.job;


import com.beyond.specguard.resume.model.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResumeCleanupScheduler {

    private final ResumeService resumeService;

    @Value("${app.cleanup.resumes.enabled:true}")
    private boolean enabled;

    @Value("${app.cleanup.resumes.batch-size:200}")
    private int batchSize;

    // 기본: 매 15분 (서울 타임존)
    @Scheduled(cron = "${app.cleanup.resumes.cron:0 0/15 * * * *}", zone = "Asia/Seoul")
    public void run() {
        if (!enabled) return;
        int deleted = resumeService.cleanupExpiredUnsubmittedResumes(batchSize);
        if (deleted > 0) {
            log.info("[cleanup] expired & unsubmitted resumes deleted: {}", deleted);
        }
    }
}